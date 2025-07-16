package com.example.storesports.core.admin.order.controller;

import com.example.storesports.entity.Order;
import com.example.storesports.entity.Payment;
import com.example.storesports.entity.Shipment;
import com.example.storesports.infrastructure.configuration.vnpay.VnPayConfig;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.PaymentStatus;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.repositories.OrderRepository;
import com.example.storesports.repositories.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final OrderRepository orderRepository;


    private final PaymentRepository paymentRepository;

    @GetMapping("/callback")
    @Transactional
    public ResponseEntity<?> handleVnpayCallbackGet(@RequestParam Map<String, String> params, HttpServletRequest request) {
        log.info("⚠️ [VNPay Callback] GET request nhận tham số: {}", params);
        return processVnpayCallback(params, request);
    }

    @PostMapping("/callback")
    @Transactional
    public ResponseEntity<?> handleVnpayCallbackPost(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> params.put(key, value[0]));

        // Log thêm body nếu có
        try {
            String body = request.getReader().lines().collect(Collectors.joining());
            log.info("🔥 [VNPay Callback POST] Body: {}", body);
        } catch (IOException e) {
            log.error("🚫 Lỗi khi đọc body POST: {}", e.getMessage(), e);
        }

        log.info("🔥 [VNPay Callback POST] Tham số: {}", params);
        return processVnpayCallback(params, request);
    }

    @RequestMapping(value = {"/callback", "/callback**"}, method = {RequestMethod.GET, RequestMethod.POST})
    @Transactional
    public ResponseEntity<?> handleVnpayCallbackFallback(HttpServletRequest request) {
        log.warn("⚠️ [VNPay Callback Fallback] Gọi fallback với URL: {}?{}", request.getRequestURL(), request.getQueryString());
        Map<String, String> params = extractParamsFromUrl(request);
        return processVnpayCallback(params, request);
    }




    private Map<String, String> extractParamsFromUrl(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        String queryString = request.getQueryString();
        String fullUrl = request.getRequestURL().toString();

        log.info("📥 [VNPay Callback] Full URL: {}", fullUrl);
        log.info("📥 [VNPay Callback] QueryString: {}", queryString);

        // Xử lý query string
        if (queryString != null && !"null".equalsIgnoreCase(queryString) && !queryString.isEmpty()) {
            try {
                for (String pair : queryString.split("&")) {
                    if (!pair.isEmpty()) {
                        String[] parts = pair.split("=", 2);
                        String key = parts[0];
                        String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                        params.put(key, value);
                    }
                }
            } catch (Exception e) {
                log.error("🚫 Lỗi khi trích xuất queryString: {}", e.getMessage(), e);
            }
        }

        // Xử lý tham số trong URL nếu query string không đầy đủ
        if (params.isEmpty() && fullUrl.contains("&")) {
            try {
                String[] fragments = fullUrl.split("\\?", 2)[0].split("&", 2);
                if (fragments.length > 1) {
                    for (String pair : fragments[1].split("&")) {
                        if (!pair.isEmpty()) {
                            String[] parts = pair.split("=", 2);
                            String key = parts[0];
                            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
                            params.put(key, value);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("🚫 Lỗi khi trích xuất tham số từ URL: {}", e.getMessage(), e);
            }
        }

        log.info("🔍 [VNPay Callback] Tham số trích xuất: {}", params);
        return params;
    }

    private ResponseEntity<?> processVnpayCallback(Map<String, String> params, HttpServletRequest request) {
        try {
            log.info("📥 [VNPay Callback] Method: {}, Params: {}", request.getMethod(), params);

            if (params == null || params.isEmpty()) {
                log.error("🚫 Không nhận được tham số từ VNPay");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseData.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Không nhận được tham số từ VNPay")
                                .build());
            }

            String vnp_SecureHash = params.remove("vnp_SecureHash");
            if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
                log.error("🚫 Thiếu chữ ký vnp_SecureHash");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseData.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Thiếu vnp_SecureHash")
                                .build());
            }

            String rawData = params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            String calculatedHash = VnPayConfig.hmacSHA512(VnPayConfig.secretKey, rawData);

            if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
                log.error("🚫 Chữ ký không hợp lệ. Raw: {}, Hash: {}", rawData, calculatedHash);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseData.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Chữ ký không hợp lệ")
                                .build());
            }

            String vnp_TxnRef = params.get("vnp_TxnRef");
            String orderCode = vnp_TxnRef.split("-")[0];
            double vnp_Amount = Double.parseDouble(params.get("vnp_Amount")) / 100;
            String vnp_ResponseCode = params.get("vnp_ResponseCode");
            String vnp_TransactionNo = params.get("vnp_TransactionNo");

            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode));

            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán cho đơn hàng: " + orderCode));

            double totalAmount = order.getOrderTotal() + order.getShipments().stream()
                    .mapToDouble(Shipment::getShippingCost).sum();

            if (Math.abs(vnp_Amount - totalAmount) > 0.01) {
                log.error("❌ Số tiền không khớp. VNPay: {}, Hệ thống: {}", vnp_Amount, totalAmount);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseData.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Số tiền thanh toán không khớp")
                                .build());
            }

            if ("00".equals(vnp_ResponseCode)) {
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setAmount(vnp_Amount);
                payment.setTransactionId(vnp_TransactionNo);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setChangeAmount(0.0);
                paymentRepository.save(payment);

//                order.setOrderStatus(OrderStatus.COMPLETED);
//                order.setDeleted(order.getIsPos());
//                orderRepository.save(order);
//                response.sendRedirect("http://localhost:8080/success?orderCode=" + URLEncoder.encode(orderCode, StandardCharsets.UTF_8));
                return ResponseEntity.ok(ResponseData.builder()
                        .status(HttpStatus.OK.value())
                        .message("Thanh toán thành công")
                        .build());
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setTransactionId(vnp_TransactionNo);
                paymentRepository.save(payment);

                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setDeleted(true);
                orderRepository.save(order);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ResponseData.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Thanh toán thất bại: " + getVnpayErrorMessage(vnp_ResponseCode))
                                .build());
            }

        } catch (Exception e) {
            log.error("💥 Lỗi hệ thống: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Lỗi hệ thống: " + e.getMessage())
                            .build());
        }
    }

    private String getVnpayErrorMessage(String code) {
        return switch (code) {
            case "07" -> "Giao dịch bị nghi ngờ gian lận";
            case "09" -> "Giao dịch không thành công do lỗi ngân hàng";
            case "10", "24" -> "Khách hàng hủy giao dịch";
            case "11" -> "Giao dịch chưa hoàn tất";
            default -> "Lỗi không xác định: " + code;
        };
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

}
