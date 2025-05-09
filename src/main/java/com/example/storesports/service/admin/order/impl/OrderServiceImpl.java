package com.example.storesports.service.admin.order.impl;


import com.example.storesports.core.admin.order.payload.CreateInvoiceRequest;
import com.example.storesports.core.admin.order.payload.OrderRequest;
import com.example.storesports.core.admin.order.payload.OrderResponse;
import com.example.storesports.core.admin.orderItem.payload.OrderItemResponse;
import com.example.storesports.core.admin.payment.payload.PaymentResponse;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.PaymentStatus;
import com.example.storesports.infrastructure.constant.Role;
import com.example.storesports.infrastructure.constant.ShipmentStatus;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.order.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;


    private final ProductRepository productRepository;

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final CouponRepository couponRepository;

    private final CouponUsageRepository couponUsageRepository;

    private final UserAddressMappingRepository userAddressMappingRepository;

    private final ShipmentRepository shipmentRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final ShipmentItemRepository shipmentItemRepository;

    @Override
    @Transactional
    public OrderResponse createInvoice(CreateInvoiceRequest request) {
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderTotal(0.0);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setIsPos(request.getIsPos());
        order.setDeleted(false);
        order.setCreatedBy(1); // Hardcoded ADMIN ID
        order.setCreatedDate(LocalDateTime.now());
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse addOrderDetails(String orderCode, OrderRequest request) {
        // 1. Tìm đơn hàng theo mã (orderCode)
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // 2. Kiểm tra mã đơn hàng có trùng với request không
        if (!orderCode.equals(request.getOrderCode())) {
            throw new IllegalArgumentException("Mã đơn hàng không khớp");
        }

        // 3. Kiểm tra dữ liệu bắt buộc
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp danh sách sản phẩm");
        }
        if (request.getPayment() == null) {
            throw new IllegalArgumentException("Vui lòng cung cấp thông tin thanh toán");
        }

        // 4. Kiểm tra logic POS và đơn giao hàng
        if (!request.getIsPos()) {
            if (request.getUserId() == null || request.getAddressId() == null || request.getShipments() == null || request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp userId, addressId và shipment cho đơn giao hàng");
            }
        } else {
            if (request.getAddressId() != null || (request.getShipments() != null && !request.getShipments().isEmpty())) {
                throw new IllegalArgumentException("Đơn POS không được chứa addressId hoặc shipment");
            }
        }

        // 5. Lấy thông tin người dùng nếu có
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
            order.setUser(user);
        }

        // 6. Kiểm tra địa chỉ giao hàng (nếu là đơn giao hàng)
        if (!request.getIsPos()) {
            userAddressMappingRepository.findByUserIdAndAddressId(request.getUserId(), request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("Địa chỉ không hợp lệ với user"));
        }

        // 7. Lấy phương thức thanh toán
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));

        // 8. Kiểm tra tồn kho sản phẩm
        for (var item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm ID: " + item.getProductId()));
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm ID: " + item.getProductId() + " không đủ hàng");
            }
        }

        // 9. Tính tổng đơn hàng
        double orderTotal = request.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        // 10. Áp dụng mã giảm giá nếu có
        Coupon coupon = null;
        if (request.getCouponId() != null) {
            coupon = couponRepository.findById(request.getCouponId())
                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));
            orderTotal -= coupon.getDiscountAmount();

            // Lưu lại việc sử dụng mã giảm giá
            CouponUsage usage = new CouponUsage();
            usage.setCoupon(coupon);
            usage.setUser(order.getUser());
//            usage.setUsedDate(LocalDateTime.now());
            couponUsageRepository.save(usage);
        }

        // 11. Cập nhật đơn hàng
        order.setIsPos(request.getIsPos());
        order.setOrderTotal(orderTotal);
        order.setOrderStatus(request.getIsPos() ? OrderStatus.COMPLETED : OrderStatus.PENDING);
        order.setLastModifiedBy(1); // giả lập Admin
        order.setLastModifiedDate(LocalDateTime.now());

        // 12. Xóa item cũ (nếu có) và thêm mới
        order.getOrderItems().clear();
        for (var item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(item.getUnitPrice());
            order.getOrderItems().add(orderItem);

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        // 13. Xử lý thanh toán


        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseThrow(() -> new IllegalArgumentException("ko tìm thấy"));
        if (payment == null) {
            payment = new Payment();
            payment.setOrder(order);
        }
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(request.getPayment().getAmount());
        boolean isCod = paymentMethod.getName().equalsIgnoreCase("COD");
        payment.setPaymentStatus(request.getIsPos() || !isCod ? PaymentStatus.COMPLETED : PaymentStatus.PENDING);
        payment.setPaymentDate(request.getIsPos() || !isCod ? LocalDateTime.now() : null);
        paymentRepository.save(payment);

        // 14. Thêm thông tin giao hàng (nếu là đơn giao hàng)
        if (!request.getIsPos()) {
            for (var shipmentReq : request.getShipments()) {
                Shipment shipment = new Shipment();
                shipment.setOrder(order);
                shipment.setCarrier(shipmentReq.getCarrier());
                shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate() != null
                        ? LocalDateTime.parse(shipmentReq.getEstimatedDeliveryDate())
                        : null);
                shipment.setShipmentStatus(ShipmentStatus.PENDING);
                shipment.setTrackingNumber(generateTrackingNumber());
                shipmentRepository.save(shipment);

                // Gán shipment với orderItem
                for (Long orderItemId : shipmentReq.getOrderItemIds()) {
                    OrderItem orderItem = order.getOrderItems().stream()
                            .filter(oi -> oi.getId().equals(orderItemId))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy OrderItem ID: " + orderItemId));
                    ShipmentItem shipmentItem = new ShipmentItem();
                    shipmentItem.setShipment(shipment);
                    shipmentItem.setOrderItem(orderItem);
                    shipmentItemRepository.save(shipmentItem);
                }
            }
        }

        // 15. Lưu đơn hàng cuối cùng
        orderRepository.save(order);

        // 16. Trả về response
        return mapToOrderResponse(order);
    }






public OrderResponse mapToOrderResponse(Order order) {
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null.");
    }

    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setOrderCode(order.getOrderCode());
    //response.setUserId(order.getUser().getId()); response.setUserId(order.getUser() != null ? order.getUser().getId() : null); //
    response.setOrderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null);
    response.setOrderTotal(order.getOrderTotal());
    response.setIsPos(order.getIsPos());
    response.setDeleted(order.getDeleted());
    response.setOrderDate(order.getOrderDate());
    response.setCreatedBy(order.getCreatedBy());
    response.setCreatedDate(order.getCreatedDate());
    response.setLastModifiedBy(order.getLastModifiedBy());
    response.setLastModifiedDate(order.getLastModifiedDate());


//    response.setItems(orderItemRepository.findByOrderId(order.getId()).stream()
//            .map(orderItem -> {
//                OrderItemResponse orderItemResponse = new OrderItemResponse();
//                    orderItemResponse.setId(orderItem.getId());
//                    orderItemResponse.setProductId(orderItem.getProduct().getId());
//                    orderItemResponse.setProductName(orderItem.getProduct().getName());
//                    orderItemResponse.setQuantity(orderItem.getQuantity());
//                    orderItemResponse.setUnitPrice(orderItemResponse.getUnitPrice());
//                     return  orderItemResponse;
//            }).collect(Collectors.toList()));
    response.setItems(orderItemRepository.findByOrderId(order.getId()).stream()
            .map(orderItem -> new OrderItemResponse(
                    orderItem.getId(),
                    orderItem.getProduct().getId(),
                    orderItem.getProduct().getName(),
                    orderItem.getQuantity(),
                    orderItem.getUnitPrice()
            ))
            .toList());

    // Map payment
    Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
    if (paymentOptional.isPresent()) {
        Payment payment = paymentOptional.get();
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(payment.getId());
        paymentResponse.setAmount(payment.getAmount());
        paymentResponse.setPaymentStatus(payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null);
        paymentResponse.setPaymentDate(payment.getPaymentDate());
        paymentResponse.setPaymentMethodName(payment.getPaymentMethod().getName());
        response.setPayment(paymentResponse);
    }


    if (order.getUser() != null && order.getUser().getId() != null) {
        List<CouponUsage> couponUsages = couponUsageRepository.findByUserId(order.getUser().getId());
        response.setCouponUsages(couponUsages.stream()
                .map(couponUsage -> new OrderResponse.CouponResponse(
                        couponUsage.getId(),
                        couponUsage.getCoupon().getCode(),
                        couponUsage.getCoupon().getDiscountAmount(),
                        couponUsage.getUsedDate(),
                        couponUsage.getCreatedBy(),
                        couponUsage.getCreatedDate(),
                        couponUsage.getLastModifiedBy(),
                        couponUsage.getLastModifiedDate()
                ))
                .toList());
    } else {
        response.setCouponUsages(new ArrayList<>());
    }

    // Map address (nếu không phải đơn hàng tại quầy)
    if (order.getIsPos() != null && !order.getIsPos() && order.getUser().getId() != null) {
        Optional<UserAddressMapping> addressMappingOptional = userAddressMappingRepository
                .findByUserId(order.getUser().getId())
                .stream()
                .findFirst(); // Giả sử lấy địa chỉ đầu tiên, cần logic thực tế
        if (addressMappingOptional.isPresent() && addressMappingOptional.get().getAddress() != null) {
            Address address = addressMappingOptional.get().getAddress();
            response.setAddress(new OrderResponse.AddressResponse(
                    address.getId(),
                    address.getStreetAddress(),
                    address.getCity(),
                    address.getState(),
                    address.getCountry(),
                    address.getZipCode()
            ));
        }
    }

    // Map shipment
    Optional<Shipment> shipmentOptional = shipmentRepository.findByOrderId(order.getId());
    if (shipmentOptional.isPresent()) {
        Shipment shipment = shipmentOptional.get();
        response.setShipment(new OrderResponse.ShipmentResponse(
                shipment.getId(),
                shipment.getShipmentDate(),
                shipment.getShipmentStatus(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getEstimatedDeliveryDate()
        ));
    }



    response.setItems(orderItemRepository.findByOrderId(order.getId()).stream()
            .map(orderItem -> {
                OrderItemResponse orderItemResponse = new OrderItemResponse();
                orderItemResponse.setId(orderItem.getId());
                orderItemResponse.setProductId(orderItem.getProduct().getId());
                orderItemResponse.setProductName(orderItem.getProduct().getName());
                orderItemResponse.setQuantity(orderItem.getQuantity());
                orderItemResponse.setUnitPrice(orderItemResponse.getUnitPrice());
                return  orderItemResponse;
            }).collect(Collectors.toList()));

    return response;
}

    private String generateOrderCode() {
        return "NAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis();
    }
////public OrderResponse mapToOrderResponseV2(Order order) {
////    if (order == null) return null;
////
////    OrderResponse response = new OrderResponse();
////    response.setId(order.getId());
////    response.setOrderCode(order.getOrderCode());
////    response.setUserId(order.getUser() != null ? order.getUser().getId() : null);
////    response.setOrderStatus(order.getOrderStatus().name());
////    response.setOrderTotal(order.getOrderTotal());
////    response.setIsPos(order.getIsPos());
////    response.setDeleted(order.getDeleted());
////    response.setOrderDate(order.getOrderDate());
////    response.setItems(mapOrderItems(order.getOrderItems()));
////    response.setPayment(mapPayment(order.getPayments()));
////    response.setCouponUsages(mapCouponUsages(order)); // Tùy bạn có bảng liên kết hay không
////    response.setAddress(mapAddress(order)); // Nếu có entity Address
////    response.setShipment(mapShipment(order.getShipments()));
////    response.setCreatedBy(order.getCreatedBy());
////    response.setCreatedDate(order.getCreatedDate());
////    response.setLastModifiedBy(order.getLastModifiedBy());
////    response.setLastModifiedDate(order.getLastModifiedDate());
////
////    return response;
////}
//    private List<OrderItemResponse> mapOrderItems(List<OrderItem> orderItems) {
//        if (orderItems == null) return List.of();
//        return orderItems.stream().map(item -> {
//            OrderItemResponse resp = new OrderItemResponse();
//            resp.setId(item.getId());
//            resp.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
//            resp.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
//            resp.setQuantity(item.getQuantity());
//            resp.setUnitPrice(item.getUnitPrice());
//            return resp;
//        }).toList();
//    }
//
//    private PaymentResponse mapPayment(List<Payment> payments) {
//        if (payments == null || payments.isEmpty()) return null;
//
//        Payment payment = payments.get(0); // Lấy payment đầu tiên
//        PaymentResponse resp = new PaymentResponse();
//        resp.setId(payment.getId());
//        resp.setAmount(payment.getAmount());
//        resp.setPaymentMethodName(payment.getPaymentMethod().getName());
//        resp.setPaymentStatus(String.valueOf(payment.getPaymentStatus()));
//        resp.setPaymentDate(payment.getPaymentDate());
//        return resp;
//    }
//
////    private OrderResponse.ShipmentResponse mapShipment(List<Shipment> shipments) {
////        if (shipments == null || shipments.isEmpty()) return null;
////
////        Shipment shipment = shipments.get(0); // Lấy shipment đầu tiên
////        return new OrderResponse.ShipmentResponse(
////                shipment.getId(),
////                shipment.getShipmentDate(),
////                shipment.getShipmentStatus().name(),
////                shipment.getTrackingNumber()
////        );
////    }
//
////    private List<OrderResponse.CouponUsageResponse> mapCouponUsages(Order order) {
////        if (order.getCoupons() == null) return List.of();
////
////        return order.getCoupons().stream().map(couponUsage -> {
////            return new OrderResponse.CouponUsageResponse(
////                    couponUsage.getId(),
////                    couponUsage.getCoupon().getCode(),
////                    couponUsage.getDiscountAmount(),
////                    couponUsage.getUsedDate(),
////                    couponUsage.getCreatedBy(),
////                    couponUsage.getCreatedDate(),
////                    couponUsage.getLastModifiedBy(),
////                    couponUsage.getLastModifiedDate()
////            );
////        }).toList();
////    }
////    private OrderResponse.AddressResponse mapAddress(Order order) {
////        if (order.getUser() == null || order.getUser().getAddres == null) return null;
////
////        Address addr = order.getUser().getAddress(); // giả định user có address
////        return new OrderResponse.AddressResponse(
////                addr.getId(),
////                addr.getStreetAddress(),
////                addr.getCity(),
////                addr.getState(),
////                addr.getCountry(),
////                addr.getZipCode()
////        );
////    }
//
//

//    @Override
//    @Transactional
//    public OrderResponse addProductToOrder(OrderRequest request) {
//        // 1. Tìm đơn hàng
//        Order order = orderRepository.findByOrderCode(request.getOrderCode())
//                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại với mã: " + request.getOrderCode()));
//
//        // Kiểm tra xem đơn hàng đã bị xóa mềm chưa
//        if (order.getDeleted()) {
//            throw new IllegalArgumentException("Đơn hàng đã bị xóa mềm: " + request.getOrderCode());
//        }
//
//        // Kiểm tra xem đơn hàng đã có sản phẩm chưa
//        if (!order.getOrderItems().isEmpty()) {
//            throw new IllegalArgumentException("Đơn hàng đã có sản phẩm, không thể thêm nữa");
//        }
//
//        // 2. Kiểm tra user (ADMIN)
//        User user = userRepository.findById(order.getUser().getId())
//                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại với ID: "));
//
//        // 3. Tạo các mục trong đơn hàng và kiểm tra tồn kho
//        Order finalOrder = order;
//        List<OrderItem> items = request.getItems().stream().map(itemRequest -> {
//            Product product = productRepository.findById(itemRequest.getProductId())
//                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại với ID: " + itemRequest.getProductId()));
//
//            // Kiểm tra tồn kho
//            if (product.getStockQuantity() < itemRequest.getQuantity()) {
//                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ tồn kho. Hiện có: " + product.getStockQuantity());
//            }
//
//            // Cập nhật tồn kho
//            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
//            product.setLastModifiedBy(user.getId().intValue());
//            product.setLastModifiedDate(LocalDateTime.now());
//            productRepository.save(product);
//
//            // Tạo order item
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrder(finalOrder); // Liên kết với order
//            orderItem.setProduct(product);
//            orderItem.setQuantity(itemRequest.getQuantity());
//            orderItem.setUnitPrice(product.getPrice());
//            orderItem.setCreatedBy(user.getId().intValue());
//            orderItem.setCreatedDate(LocalDateTime.now());
//            orderItem.setLastModifiedBy(user.getId().intValue());
//            orderItem.setLastModifiedDate(LocalDateTime.now());
//            return orderItem;
//        }).collect(Collectors.toList());
//
//        // Gán danh sách orderItems cho order
//        order.getOrderItems().clear(); // Xóa danh sách cũ (nếu có)
//        order.getOrderItems().addAll(items);
//
//        // Lưu order trước để lấy ID (nếu cần)
//        try {
//            order = orderRepository.saveAndFlush(order);
//            System.out.println("Order saved with ID: " + order.getId() + ", OrderItems count: " + order.getOrderItems().size());
//        } catch (Exception e) {
//            System.err.println("Error saving order: " + e.getMessage());
//            throw new RuntimeException("Failed to save order and order items", e);
//        }
//
//        // Lưu từng OrderItem riêng lẻ (để chắc chắn)
//        try {
//            for (OrderItem item : items) {
//                orderItemRepository.save(item);
//            }
//            System.out.println("OrderItems saved individually: " + items.size());
//        } catch (Exception e) {
//            System.err.println("Error saving order items: " + e.getMessage());
//            throw new RuntimeException("Failed to save order items", e);
//        }
//
//        // 4. Tính tổng tiền trước giảm giá
//        Double orderTotal = items.stream()
//                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
//                .sum();
//
//        // 5. Áp dụng giảm giá bằng danh sách coupon (nếu có)
//        Double finalTotal = orderTotal;
//        List<CouponUsage> couponUsages = new ArrayList<>();
//        if (request.getCouponCodes() != null && !request.getCouponCodes().isEmpty()) {
//            for (String couponCode : request.getCouponCodes()) {
//                Coupon coupon = couponRepository.findByCode(couponCode)
//                        .orElseThrow(() -> new IllegalArgumentException("Mã coupon không tồn tại: " + couponCode));
//
//                // Áp dụng giảm giá
//                finalTotal = Math.max(0, finalTotal - coupon.getDiscountAmount());
//
//                // Lưu lịch sử sử dụng coupon
//                CouponUsage couponUsage = new CouponUsage();
//                couponUsage.setCoupon(coupon);
//                couponUsage.setUser(user);
//                //couponUsage.setUsedDate(LocalDateTime.now());
//                couponUsage.setCreatedBy(user.getId().intValue());
//                couponUsage.setCreatedDate(LocalDateTime.now());
//                couponUsage.setLastModifiedBy(user.getId().intValue());
//                couponUsage.setLastModifiedDate(LocalDateTime.now());
//                couponUsages.add(couponUsage);
//            }
//            try {
//                couponUsageRepository.saveAll(couponUsages);
//                System.out.println("Coupon usages saved: " + couponUsages.size());
//            } catch (Exception e) {
//                System.err.println("Error saving coupon usages: " + e.getMessage());
//                throw new RuntimeException("Failed to save coupon usages", e);
//            }
//        }
//
//        order.setOrderTotal(finalTotal);
//        order.setOrderStatus(OrderStatus.COMPLETED);
//        order.setLastModifiedBy(user.getId().intValue());
//        order.setLastModifiedDate(LocalDateTime.now());
//
//        // Lưu lại order sau khi cập nhật
//        try {
//            order = orderRepository.save(order);
//            System.out.println("Order updated with total: " + order.getOrderTotal());
//        } catch (Exception e) {
//            System.err.println("Error updating order: " + e.getMessage());
//            throw new RuntimeException("Failed to update order", e);
//        }
//
//        // 6. Tạo thanh toán
//        Payment payment = new Payment();
//        payment.setOrder(order);
//        payment.setAmount(finalTotal);
//        payment.setPaymentStatus(PaymentStatus.COMPLETED);
//        payment.setPaymentDate(order.getOrderDate());
//        payment.setCreatedBy(user.getId().intValue());
//        payment.setCreatedDate(LocalDateTime.now());
//        payment.setLastModifiedBy(user.getId().intValue());
//        payment.setLastModifiedDate(LocalDateTime.now());
//        try {
//            paymentRepository.save(payment);
//            System.out.println("Payment saved with ID: " + payment.getId());
//        } catch (Exception e) {
//            System.err.println("Error saving payment: " + e.getMessage());
//            throw new RuntimeException("Failed to save payment", e);
//        }
//
//        // 7. Tạo response
//        return mapToOrderResponse(order);
//    }
//


}
