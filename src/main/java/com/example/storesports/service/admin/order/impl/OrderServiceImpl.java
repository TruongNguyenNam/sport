package com.example.storesports.service.admin.order.impl;


import com.example.storesports.core.admin.order.payload.*;
import com.example.storesports.core.admin.orderItem.payload.OrderItemResponse;
import com.example.storesports.core.admin.payment.payload.PaymentResponse;
import com.example.storesports.core.admin.shipment.payload.ShipmentResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.*;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.order.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.*;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;

    private final PaymentRepository paymentRepository;

    private final UserRepository userRepository;

    private final CouponRepository couponRepository;

    private final CouponUsageRepository couponUsageRepository;

    private final UserAddressMappingRepository userAddressMappingRepository;

    private final AddressRepository addressRepository;

    private final ShipmentRepository shipmentRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final ShipmentItemRepository shipmentItemRepository;

    private final CarrierRepository carrierRepository;


    @Transactional
    @Override
    public OrderResponse addProductToOrderV2(OrderRequest request) {
        Order order = orderRepository.findByOrderCode(request.getOrderCode())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + request.getOrderCode()));

        if (!order.getOrderCode().equals(request.getOrderCode())) {
            throw new IllegalArgumentException("Mã đơn hàng không khớp");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp danh sách sản phẩm");
        }
        if (request.getPayment() == null) {
            throw new IllegalArgumentException("Vui lòng cung cấp thông tin thanh toán");
        }

        if (!order.getIsPos()) {
            if (request.getUserId() == null || request.getShipments() == null || request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp userId và thông tin shipment cho đơn giao hàng");
            }
        } else {
            if (request.getShipments() != null && !request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Đơn POS không được chứa shipment");
            }
        }

        // userId
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
            log.info("Khách hàng đã đăng ký: {}", user.getId());
        } else if (order.getIsPos()) {
            log.info("Khách vãng lai cho đơn POS");
        } else {
            throw new IllegalArgumentException("Đơn ship yêu cầu userId");
        }
        order.setUser(user);

        // Logic thêm sản phẩm
        double totalAmount = 0.0;
        double totalShippingCost = 0.0;
        List<OrderItem> savedOrderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng tồn kho");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            log.info("Số tiền của sản phẩm {}: {}", product.getName(), product.getPrice());
            OrderItem savedItem = orderItemRepository.save(orderItem);

            savedOrderItems.add(savedItem);
            totalAmount += savedItem.getQuantity() * savedItem.getUnitPrice();

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Xử lý phí vận chuyển cho đơn giao hàng
        if (!order.getIsPos()) {
            List<OrderRequest.ShipmentRequest> shipmentRequests = request.getShipments();
            if (shipmentRequests == null || shipmentRequests.isEmpty()) {
                throw new IllegalArgumentException("Danh sách shipment không được để trống đối với đơn giao hàng.");
            }

            for (OrderRequest.ShipmentRequest shipmentReq : shipmentRequests) {
                if (shipmentReq.getShippingCost() == null || shipmentReq.getShippingCost() < 0) {
                    throw new IllegalArgumentException("Phí vận chuyển không hợp lệ cho shipment với carrier ID: " + shipmentReq.getCarrierId());
                }
                totalShippingCost += shipmentReq.getShippingCost();
                log.info("Phí vận chuyển cho shipment với carrier ID {}: {}", shipmentReq.getCarrierId(), shipmentReq.getShippingCost());
            }
        }

        // Gán tổng tiền của đơn hàng (bao gồm phí vận chuyển)
        order.setOrderTotal(totalAmount + totalShippingCost);
        log.info("Tổng số tiền trước khi áp dụng coupon (bao gồm phí vận chuyển): {}", order.getOrderTotal());

        // Apply coupon if provided
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại");
            }
            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                if (user == null || !usage.getUser().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này");
                }

                Coupon coupon = usage.getCoupon();
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn");
                }

                totalDiscount += coupon.getDiscountAmount();
                log.info("Áp dụng mã giảm giá: {}, giảm: {}", coupon.getCodeCoupon(), coupon.getDiscountAmount());
                usage.setDeleted(true);
                couponUsageRepository.save(usage);
            }

            order.setOrderTotal(order.getOrderTotal() - totalDiscount);
            if (order.getOrderTotal() < 0) {
                order.setOrderTotal(0.0);
            }
            orderRepository.save(order);
        }

        log.info("Tổng số tiền sau khi áp dụng coupon: {}", order.getOrderTotal());

        // Thanh toán
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
        Payment payment = paymentOptional.orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setOrder(order);
            return newPayment;
        });
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
        payment.setPaymentMethod(paymentMethod);
        double paidAmount = request.getPayment().getAmount();
        log.info("Số tiền khách đưa: {}", paidAmount);
        payment.setAmount(paidAmount);
        payment.setChangeAmount(paidAmount - order.getOrderTotal());
        payment.setPaymentDate(LocalDateTime.now());

        // Set trạng thái thanh toán và deleted dựa trên loại đơn hàng
        if (order.getIsPos()) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setDeleted(true);
        } else {
            payment.setPaymentStatus(PaymentStatus.PENDING);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setDeleted(false);
        }
        paymentRepository.save(payment);

        // Xử lý vận chuyển cho đơn giao hàng
        if (!order.getIsPos()) {
            List<OrderRequest.ShipmentRequest> shipmentRequests = request.getShipments();
            for (OrderRequest.ShipmentRequest shipmentReq : shipmentRequests) {
                Shipment shipment = new Shipment();
                shipment.setOrder(order);
                Carrier carrier = carrierRepository.findById(shipmentReq.getCarrierId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển với ID: " + shipmentReq.getCarrierId()));
                shipment.setCarrier(carrier);
                shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
                shipment.setShipmentStatus(ShipmentStatus.PENDING);
                shipment.setTrackingNumber(generateTrackingNumber());
                shipment.setShipmentDate(new Date());
                shipment.setShippingCost(shipmentReq.getShippingCost());
                shipment.setDeleted(false);
                Shipment savedShipment = shipmentRepository.save(shipment);

                for (OrderItem item : savedOrderItems) {
                    ShipmentItem shipmentItem = new ShipmentItem();
                    shipmentItem.setShipment(savedShipment);
                    shipmentItem.setOrderItem(item);
                    shipmentItemRepository.save(shipmentItem);
                }
            }
        }

        order.setNodes(request.getNodes());
        order.setOrderDate(new Date());
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Transactional
    @Override
    public OrderResponse addProductToOrderV3(OrderRequest request) {
        Order order = orderRepository.findByOrderCode(request.getOrderCode())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + request.getOrderCode()));

        if (!order.getOrderCode().equals(request.getOrderCode())) {
            throw new IllegalArgumentException("Mã đơn hàng không khớp");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp danh sách sản phẩm");
        }

        // Kiểm tra userId và shipment
        if (!order.getIsPos()) {
            if (request.getUserId() == null || request.getShipments() == null || request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp userId và thông tin shipment cho đơn giao hàng");
            }
        } else {
            if (request.getShipments() != null && !request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Đơn POS không được chứa shipment");
            }
        }

        // Xử lý userId
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
            log.info("Khách hàng đã đăng ký: {}", user.getId());
        } else if (order.getIsPos()) {
            log.info("Khách vãng lai cho đơn POS");
        } else {
            throw new IllegalArgumentException("Đơn ship yêu cầu userId");
        }
        order.setUser(user);

        // Xử lý OrderItem
        double totalAmount = 0.0;
        List<OrderItem> savedOrderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng tồn kho");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            log.info("Số tiền của sản phẩm: {}", product.getPrice());
            OrderItem savedItem = orderItemRepository.save(orderItem);

            savedOrderItems.add(savedItem);
            totalAmount += savedItem.getQuantity() * savedItem.getUnitPrice();

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Gán tổng tiền đơn hàng
        order.setOrderTotal(totalAmount);

        // Áp dụng coupon nếu có
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại");
            }
            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                if (user == null || !usage.getUser().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này");
                }

                Coupon coupon = usage.getCoupon();
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn");
                }

                totalDiscount += coupon.getDiscountAmount();
                log.info("Áp dụng mã giảm giá: {}, giảm: {}", coupon.getCodeCoupon(), coupon.getDiscountAmount());
                usage.setDeleted(true);
                couponUsageRepository.save(usage);
            }

            order.setOrderTotal(order.getOrderTotal() - totalDiscount);
            if (order.getOrderTotal() < 0) {
                order.setOrderTotal(0.0);
            }
            orderRepository.save(order);
        }

        log.info("Tổng số tiền sau khi áp dụng coupon: {}", order.getOrderTotal());

        // Xử lý thanh toán
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
        Payment payment = paymentOptional.orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setOrder(order);
            return newPayment;
        });

        if (order.getIsPos()) {
            // Đơn POS yêu cầu thanh toán ngay
            if (request.getPayment() == null) {
                throw new IllegalArgumentException("Vui lòng cung cấp thông tin thanh toán cho đơn POS");
            }
            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                    .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
            if (paymentMethod.getDeleted()) {
                throw new IllegalArgumentException("Phương thức thanh toán đã bị xóa");
            }
            payment.setPaymentMethod(paymentMethod);
            Double paidAmount = request.getPayment().getAmount();
            if (paidAmount == null || paidAmount < order.getOrderTotal()) {
                throw new IllegalArgumentException("Số tiền thanh toán không đủ hoặc không được cung cấp");
            }
            log.info("Số tiền khách đưa: {}", paidAmount);
            payment.setAmount(paidAmount);
            payment.setChangeAmount(paidAmount - order.getOrderTotal());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setDeleted(true);
        } else {
            // Đơn ship có thể thanh toán ngay hoặc COD
            if (request.getPayment() != null) {
                PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                        .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
//                if (paymentMethod.getDeleted()) {
//                    throw new IllegalArgumentException("Phương thức thanh toán đã bị xóa");
//                }
                payment.setPaymentMethod(paymentMethod);
                if (!paymentMethod.getName().equals("COD")) {
                    // Thanh toán ngay
                    Double paidAmount = request.getPayment().getAmount();
                    if (paidAmount == null || paidAmount < order.getOrderTotal()) {
                        throw new IllegalArgumentException("Số tiền thanh toán không đủ hoặc không được cung cấp");
                    }
                    log.info("Số tiền khách đưa: {}", paidAmount);
                    payment.setAmount(paidAmount);
                    payment.setChangeAmount(paidAmount - order.getOrderTotal());
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                } else {
                    // Thanh toán COD
                    payment.setAmount(0.0);
                    payment.setChangeAmount(0.0);
                    payment.setPaymentStatus(PaymentStatus.PENDING); // Hoặc AWAITING_DELIVERY nếu có
                }
            } else {
                // Mặc định COD nếu không có thông tin thanh toán
                PaymentMethod paymentMethod = paymentMethodRepository.findByName("COD")
                        .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán COD không tồn tại"));
//                if (paymentMethod.getDeleted()) {
//                    throw new IllegalArgumentException("Phương thức thanh toán COD đã bị xóa");
//                }
                payment.setPaymentMethod(paymentMethod);
                payment.setAmount(0.0);
                payment.setChangeAmount(0.0);
                payment.setPaymentStatus(PaymentStatus.PENDING); // Hoặc AWAITING_DELIVERY
            }
            order.setOrderStatus(OrderStatus.PENDING);
            order.setDeleted(false);
        }
        paymentRepository.save(payment);

        // Xử lý vận chuyển cho đơn giao hàng
        if (!order.getIsPos()) {
            List<OrderRequest.ShipmentRequest> shipmentRequests = request.getShipments();
            if (shipmentRequests == null || shipmentRequests.isEmpty()) {
                throw new IllegalArgumentException("Danh sách shipment không được để trống đối với đơn giao hàng.");
            }

            for (OrderRequest.ShipmentRequest shipmentReq : shipmentRequests) {
                Shipment shipment = new Shipment();
                shipment.setOrder(order);
                Carrier carrier = carrierRepository.findById(shipmentReq.getCarrierId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển với ID: " + shipmentReq.getCarrierId()));
                shipment.setCarrier(carrier);
                shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
                shipment.setShipmentStatus(ShipmentStatus.PENDING);
                shipment.setTrackingNumber(generateTrackingNumber());
                shipment.setShipmentDate(new Date());
                shipment.setDeleted(false);
                Shipment savedShipment = shipmentRepository.save(shipment);

                // Liên kết OrderItem với Shipment
                if (shipmentReq.getOrderItemIds() != null && !shipmentReq.getOrderItemIds().isEmpty()) {
                    for (Long orderItemId : shipmentReq.getOrderItemIds()) {
                        OrderItem orderItem = savedOrderItems.stream()
                                .filter(item -> item.getId().equals(orderItemId))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy OrderItem với ID: " + orderItemId));
                        ShipmentItem shipmentItem = new ShipmentItem();
                        shipmentItem.setShipment(savedShipment);
                        shipmentItem.setOrderItem(orderItem);
                        shipmentItemRepository.save(shipmentItem);
                    }
                } else {
                    // Mặc định liên kết tất cả OrderItem nếu orderItemIds rỗng
                    for (OrderItem item : savedOrderItems) {
                        ShipmentItem shipmentItem = new ShipmentItem();
                        shipmentItem.setShipment(savedShipment);
                        shipmentItem.setOrderItem(item);
                        shipmentItemRepository.save(shipmentItem);
                    }
                }
            }
        }

        order.setNodes(request.getNodes());
        order.setOrderDate(new Date());
        order.setOrderStatus(OrderStatus.PENDING);
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }
    @Transactional
    @Override
    public OrderResponse updateOrder(UpdateOrderRequest request) {
        log.info("Bắt đầu cập nhật đơn hàng: {}", request.getOrderCode());

        // Tìm đơn hàng
        Order order = orderRepository.findByOrderCode(request.getOrderCode())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + request.getOrderCode()));

        // Kiểm tra trạng thái đơn hàng
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể cập nhật đơn hàng ở trạng thái PENDING");
        }

        // Kiểm tra đầu vào
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp danh sách sản phẩm để cập nhật");
        }

        // Xác thực người dùng
        User user = order.getUser();
        if (request.getUserId() != null && (user == null || !request.getUserId().equals(user.getId()))) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
            order.setUser(user);
            log.info("Cập nhật khách hàng: {}", user.getId());
        } else if (user == null && !order.getIsPos()) {
            throw new IllegalArgumentException("Đơn ship yêu cầu userId");
        }

        // Kiểm tra tồn kho trước
        log.info("Kiểm tra tồn kho cho {} sản phẩm", request.getItems().size());
        Set<Long> requestProductIds = request.getItems().stream()
                .map(UpdateOrderRequest.OrderItemUpdate::getProductId)
                .collect(Collectors.toSet());
        for (UpdateOrderRequest.OrderItemUpdate itemUpdate : request.getItems()) {
            Product product = productRepository.findById(itemUpdate.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemUpdate.getProductId()));
            Optional<OrderItem> existingItem = order.getOrderItems().stream()
                    .filter(item -> item.getProduct().getId().equals(itemUpdate.getProductId()))
                    .findFirst();
            int currentQuantity = existingItem.map(OrderItem::getQuantity).orElse(0);
            int quantityChange = itemUpdate.getQuantity() - currentQuantity;
            if (quantityChange > 0 && product.getStockQuantity() < quantityChange) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng tồn kho");
            }
        }

        // Xóa các OrderItem không còn trong danh sách yêu cầu
        List<OrderItem> currentOrderItems = new ArrayList<>(order.getOrderItems());
        for (OrderItem orderItem : currentOrderItems) {
            if (!requestProductIds.contains(orderItem.getProduct().getId())) {
                // Xóa ShipmentItem liên quan trước
                try {
                    shipmentItemRepository.deleteByOrderItemId(orderItem.getId());
                } catch (Exception e) {
                    log.error("Lỗi khi xóa ShipmentItem cho orderItemId {}: {}", orderItem.getId(), e.getMessage());
                    throw new IllegalArgumentException("Không thể xóa ShipmentItem cho sản phẩm: " + orderItem.getProduct().getName());
                }
                Product product = orderItem.getProduct();
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                orderItemRepository.delete(orderItem);
                productRepository.save(product);
                log.info("Xóa sản phẩm {} khỏi đơn hàng", product.getName());
            }
        }

        // Xử lý sản phẩm
        double totalAmount = 0.0;
        List<OrderItem> updatedOrderItems = new ArrayList<>();
        for (UpdateOrderRequest.OrderItemUpdate itemUpdate : request.getItems()) {
            Product product = productRepository.findById(itemUpdate.getProductId()).get();
            Optional<OrderItem> existingItem = order.getOrderItems().stream()
                    .filter(item -> item.getProduct().getId().equals(itemUpdate.getProductId()))
                    .findFirst();

            if (itemUpdate.getQuantity() == 0) {
                if (existingItem.isPresent()) {
                    OrderItem orderItem = existingItem.get();
                    // Xóa ShipmentItem liên quan trước
                    try {
                        shipmentItemRepository.deleteByOrderItemId(orderItem.getId());
                    } catch (Exception e) {
                        log.error("Lỗi khi xóa ShipmentItem cho orderItemId {}: {}", orderItem.getId(), e.getMessage());
                        throw new IllegalArgumentException("Không thể xóa ShipmentItem cho sản phẩm: " + orderItem.getProduct().getName());
                    }
                    product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                    orderItemRepository.delete(orderItem);
                    productRepository.save(product);
                    log.info("Xóa sản phẩm {} khỏi đơn hàng", product.getName());
                }
                continue;
            }

            OrderItem orderItem;
            if (existingItem.isPresent()) {
                orderItem = existingItem.get();
                int oldQuantity = orderItem.getQuantity();
                orderItem.setQuantity(itemUpdate.getQuantity());
                orderItem.setUnitPrice(product.getPrice());
                product.setStockQuantity(product.getStockQuantity() + oldQuantity - itemUpdate.getQuantity());
                log.info("Cập nhật sản phẩm {}: số lượng cũ {}, mới {}", product.getName(), oldQuantity, itemUpdate.getQuantity());
            } else {
                orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(itemUpdate.getQuantity());
                orderItem.setUnitPrice(product.getPrice());
                product.setStockQuantity(product.getStockQuantity() - itemUpdate.getQuantity());
                log.info("Thêm mới sản phẩm {}: số lượng {}", product.getName(), itemUpdate.getQuantity());
            }

            OrderItem savedItem = orderItemRepository.save(orderItem);
            updatedOrderItems.add(savedItem);
            totalAmount += savedItem.getQuantity() * savedItem.getUnitPrice();
            productRepository.save(product);
        }

        order.setOrderItems(updatedOrderItems);
        order.setOrderTotal(totalAmount);
        log.info("Tổng tiền trước giảm giá: {}", totalAmount);

        // Áp dụng coupon
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại");
            }
            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                if (user == null || !usage.getUser().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này");
                }
                Coupon coupon = usage.getCoupon();
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn");
                }
                totalDiscount += coupon.getDiscountAmount();
                usage.setDeleted(true);
                couponUsageRepository.save(usage);
            }
            order.setOrderTotal(order.getOrderTotal() - totalDiscount);
            if (order.getOrderTotal() < 0) {
                order.setOrderTotal(0.0);
            }
            log.info("Tổng tiền sau khi áp dụng coupon: {}", order.getOrderTotal());
        }

        // Cập nhật thanh toán
        if (request.getPayment() != null) {
            Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
            Payment payment = paymentOptional.orElseGet(() -> {
                Payment newPayment = new Payment();
                newPayment.setOrder(order);
                return newPayment;
            });
            PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                    .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
            payment.setPaymentMethod(paymentMethod);
            double paidAmount = request.getPayment().getAmount();
            if (paidAmount < order.getOrderTotal()) {
                throw new IllegalArgumentException("Số tiền thanh toán không đủ: " + paidAmount + " < " + order.getOrderTotal());
            }
            log.info("Cập nhật thanh toán: số tiền khách đưa {}, tiền thừa {}", paidAmount, paidAmount - order.getOrderTotal());
            payment.setAmount(paidAmount);
            payment.setChangeAmount(paidAmount - order.getOrderTotal());
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus(order.getIsPos() ? PaymentStatus.COMPLETED : PaymentStatus.PENDING);
            paymentRepository.save(payment);
        }

        // Cập nhật vận chuyển
        if (!order.getIsPos() && request.getShipments() != null && !request.getShipments().isEmpty()) {
            // Xóa tất cả ShipmentItem và Shipment liên quan đến order
            try {
                shipmentItemRepository.deleteByOrderId(order.getId());
                shipmentRepository.deleteByOrderId(order.getId());
            } catch (Exception e) {
                log.error("Lỗi khi xóa ShipmentItem/Shipment cho orderId {}: {}", order.getId(), e.getMessage());
                throw new IllegalArgumentException("Không thể xóa thông tin vận chuyển: " + e.getMessage());
            }

            // Tạo Shipment mới
            List<Shipment> newShipments = new ArrayList<>();
            for (UpdateOrderRequest.ShipmentRequest shipmentReq : request.getShipments()) {
                Shipment shipment = new Shipment();
                shipment.setOrder(order);
                Carrier carrier = carrierRepository.findById(shipmentReq.getCarrierId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển với ID: " + shipmentReq.getCarrierId()));
                shipment.setCarrier(carrier);
                shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
                shipment.setShipmentStatus(ShipmentStatus.PENDING);
                shipment.setTrackingNumber(generateTrackingNumber());
                shipment.setShipmentDate(new Date());
                shipment.setDeleted(false);
                Shipment savedShipment = shipmentRepository.save(shipment);
                for (OrderItem item : updatedOrderItems) {
                    ShipmentItem shipmentItem = new ShipmentItem();
                    shipmentItem.setShipment(savedShipment);
                    shipmentItem.setOrderItem(item);
                    shipmentItemRepository.save(shipmentItem);
                }
                newShipments.add(savedShipment);
            }
            order.setShipments(newShipments);
            log.info("Cập nhật vận chuyển cho đơn hàng");
        }

        // Cập nhật ghi chú
        if (request.getNodes() != null) {
            order.setNodes(request.getNodes());
            log.info("Cập nhật ghi chú: {}", request.getNodes());
        }

        order.setLastModifiedDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);
        log.info("Hoàn tất cập nhật đơn hàng: {}", order.getOrderCode());

        return mapToOrderResponse(savedOrder);
    }

    @Transactional
    @Override
    public OrderResponse editOrderItems(String code, OrderRequest request) {
        Order order = orderRepository.findByOrderCode(code).orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy mã code của đơn hàng này: " + code)
        );

        // Kiểm tra trạng thái đơn hàng
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể chỉnh sửa đơn hàng ở trạng thái PENDING");
        }

        // Kiểm tra userId
        if (!order.getUser().getId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Đơn hàng không thuộc về userId: " + request.getUserId());
        }

        // Kiểm tra và hợp nhất các OrderItemRequest trùng productId
        Map<Long, Integer> mergedItems = new HashMap<>();
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Long productId = itemRequest.getProductId();
            Integer quantity = itemRequest.getQuantity();
            if (productId == null || quantity < 0) {
                throw new IllegalArgumentException("productId không hợp lệ hoặc quantity không được âm");
            }
            mergedItems.merge(productId, quantity, Integer::sum);
        }

        // Lấy danh sách OrderItem hiện tại và khôi phục tồn kho
        List<OrderItem> existingItems = orderItemRepository.findByOrderId(order.getId());
        List<Product> productsToUpdate = new ArrayList<>();
        for (OrderItem item : existingItems) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productsToUpdate.add(product);
        }
        productRepository.saveAll(productsToUpdate);

        // Xóa tất cả ShipmentItem và Shipment trước
        shipmentItemRepository.deleteByOrderId(order.getId());
        shipmentRepository.deleteByOrderId(order.getId());

        // Xóa tất cả OrderItem hiện tại
        orderItemRepository.deleteByOrderId(order.getId());

        // Thêm hoặc cập nhật OrderItem
        double totalAmount = 0.0;
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : mergedItems.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == 0) {
                continue;
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));

            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng tồn kho");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            log.info("Sản phẩm: {}, Số lượng: {}, Giá: {}", product.getName(), quantity, product.getPrice());

            OrderItem savedItem = orderItemRepository.save(orderItem);
            savedOrderItems.add(savedItem);
            totalAmount += savedItem.getQuantity() * savedItem.getUnitPrice();

            product.setStockQuantity(product.getStockQuantity() - quantity);
            productsToUpdate.add(product);
        }
        productRepository.saveAll(productsToUpdate);

        // Cập nhật tổng tiền đơn hàng
        order.setOrderTotal(totalAmount);

        // Áp dụng coupon nếu có
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại");
            }
            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                if (order.getUser() == null || !usage.getUser().getId().equals(order.getUser().getId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này");
                }
                Coupon coupon = usage.getCoupon();
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn");
                }
                totalDiscount += coupon.getDiscountAmount();
                log.info("Áp dụng mã giảm giá: {}, giảm: {}", coupon.getCodeCoupon(), coupon.getDiscountAmount());
                usage.setDeleted(true);
                couponUsageRepository.save(usage);
            }
            order.setOrderTotal(Math.max(0.0, order.getOrderTotal() - totalDiscount));
        }

        // Cập nhật thanh toán
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
        Payment payment = paymentOptional.orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setOrder(order);
            return newPayment;
        });
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
        payment.setPaymentMethod(paymentMethod);
        double paidAmount = request.getPayment().getAmount();
        log.info("Số tiền khách đưa: {}, Tổng tiền đơn hàng: {}", paidAmount, order.getOrderTotal());

        if (paidAmount < order.getOrderTotal()) {
            throw new IllegalArgumentException("Số tiền thanh toán không đủ: cần " + order.getOrderTotal());
        }

        payment.setAmount(paidAmount);
        payment.setChangeAmount(paidAmount - order.getOrderTotal());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        // Cập nhật shipment
        if (!order.getIsPos()) {
            List<OrderRequest.ShipmentRequest> shipmentRequests = request.getShipments();
            if (shipmentRequests == null || shipmentRequests.isEmpty()) {
                throw new IllegalArgumentException("Danh sách shipment không được để trống đối với đơn giao hàng.");
            }

            for (OrderRequest.ShipmentRequest shipmentReq : shipmentRequests) {
                Shipment shipment = new Shipment();
                shipment.setOrder(order);
                Carrier carrier = carrierRepository.findById(shipmentReq.getCarrierId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển với ID: " + shipmentReq.getCarrierId()));
                shipment.setCarrier(carrier);
                shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
                shipment.setShipmentStatus(ShipmentStatus.PENDING);
                shipment.setTrackingNumber(generateTrackingNumber());
                shipment.setShipmentDate(new Date());
                shipment.setDeleted(false);
                Shipment savedShipment = shipmentRepository.save(shipment);

                List<Long> orderItemIds = shipmentReq.getOrderItemIds() != null ? shipmentReq.getOrderItemIds() : new ArrayList<>();
                if (orderItemIds.isEmpty()) {
                    for (OrderItem item : savedOrderItems) {
                        ShipmentItem shipmentItem = new ShipmentItem();
                        shipmentItem.setShipment(savedShipment);
                        shipmentItem.setOrderItem(item);
                        shipmentItemRepository.save(shipmentItem);
                    }
                } else {
                    for (Long orderItemId : orderItemIds) {
                        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy OrderItem với ID: " + orderItemId));
                        if (!orderItem.getOrder().getId().equals(order.getId())) {
                            throw new IllegalArgumentException("OrderItem " + orderItemId + " không thuộc đơn hàng này");
                        }
                        ShipmentItem shipmentItem = new ShipmentItem();
                        shipmentItem.setShipment(savedShipment);
                        shipmentItem.setOrderItem(orderItem);
                        shipmentItemRepository.save(shipmentItem);
                    }
                }
            }
        }

        order.setNodes(request.getNodes());
        order.setOrderDate(new Date());
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    // LẤY TẤT CẢ DANH SÁCH HOÁ ĐƠN
    @Override
    @Transactional
    public List<OrderResponse> getAllByShip() {
        List<Order> order = orderRepository.getAllOrder();
        return order.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public  List<OrderResponse> getAllOrderStatus(OrderStatus orderStatus){
        List<Order> order = orderRepository.findByOrderStatus(orderStatus);
        return order.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }


    @Transactional
    @Override
    public OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus, String nodes) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // Kiểm tra xem đơn hàng có phải là đơn POS không
        if (order.getIsPos()) {
            throw new IllegalArgumentException("Đơn hàng tại quầy không cần cập nhật trạng thái vận chuyển");
        }

        // Kiểm tra đơn hàng đã bị xóa
        if (order.getDeleted()) {
            throw new IllegalArgumentException("Đơn hàng đã bị đánh dấu xóa, không thể cập nhật trạng thái");
        }

        // Kiểm tra tính hợp lệ của chuyển đổi trạng thái
        OrderStatus currentStatus = order.getOrderStatus();
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new IllegalArgumentException(
                    String.format("Không thể chuyển từ trạng thái %s sang %s", currentStatus, newStatus));
        }

        // Nếu hủy đơn hàng, khôi phục số lượng sản phẩm
        if (newStatus == OrderStatus.CANCELLED) {
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());
            List<Product> productsToUpdate = new ArrayList<>();
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productsToUpdate.add(product);
                log.info("Khôi phục {} sản phẩm {} cho đơn hàng {}", item.getQuantity(), product.getName(), orderCode);
            }
            productRepository.saveAll(productsToUpdate);
        }

        // Cập nhật trạng thái đơn hàng
        order.setOrderStatus(newStatus);

        // Cập nhật trạng thái shipment dựa trên trạng thái đơn hàng
        List<Shipment> shipments = shipmentRepository.findAllByOrderId(order.getId());
        if (!shipments.isEmpty()) {
            for (Shipment shipment : shipments) {
                if (newStatus == OrderStatus.SHIPPED) {
                    shipment.setShipmentStatus(ShipmentStatus.SHIPPED);
                    log.info("Cập nhật trạng thái shipment cho đơn hàng {} sang SHIPPED", orderCode);
                } else if (newStatus == OrderStatus.COMPLETED) {
                    shipment.setShipmentStatus(ShipmentStatus.DELIVERED);
                    log.info("Cập nhật trạng thái shipment cho đơn hàng {} sang DELIVERED", orderCode);
                }
                shipmentRepository.save(shipment);
            }
        } else {
            log.warn("Không tìm thấy shipment cho đơn hàng {}", orderCode);
        }

        // Nếu trạng thái mới là COMPLETED thì set deleted = true
        if (newStatus == OrderStatus.COMPLETED) {
            order.setDeleted(true);
            log.info("Đơn hàng {} đã hoàn thành, cập nhật deleted = true", orderCode);
        }

        // Ghi chú nếu có
        if (nodes != null && !nodes.trim().isEmpty()) {
            order.setNodes(nodes);
            log.info("Cập nhật ghi chú cho đơn hàng {}: {}", orderCode, nodes);
        }

        // Cập nhật thời gian sửa đổi
        order.setLastModifiedDate(LocalDateTime.now());

        orderRepository.save(order);
        log.info("Cập nhật trạng thái đơn hàng {} sang {} thành công", orderCode, newStatus);

        return mapToOrderResponse(order);
    }

    // Other methods and dependencies remain unchanged


    // Phương thức kiểm tra tính hợp lệ của chuyển đổi trạng thái
    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return false; // Không cho phép chuyển sang cùng trạng thái
        }
        switch (currentStatus) {
            case PENDING:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED:
                return newStatus == OrderStatus.COMPLETED;
            case COMPLETED:
            case CANCELLED:
            case RETURNED:
                return false; // Không cho phép chuyển đổi từ các trạng thái này
            default:
                return false;
        }
    }




    @Override
    public List<MonthlyOrderTypeResponse> getMonthlyOrderChart() {
        List<MonthlyOrderTypeProjection> projections = orderRepository.getMonthlyOrderTypeStats();

        Map<Integer, MonthlyOrderTypeResponse> result = new HashMap<>();

        for (MonthlyOrderTypeProjection p : projections) {
            int month = p.getMonth();
            boolean isPos = Boolean.TRUE.equals(p.getIsPos());
            Long total = p.getTotalOrders();

            // Nếu chưa có tháng này, tạo mới
            MonthlyOrderTypeResponse data = result.getOrDefault(month, new MonthlyOrderTypeResponse(month, 0L, 0L));

            if (isPos) {
                data.setPosOrders(total); // bán thường
            } else {
                data.setShipOrders(total); // bán ship
            }

            result.put(month, data);
        }

        return result.values().stream()
                .sorted(Comparator.comparingInt(MonthlyOrderTypeResponse::getMonth))
                .collect(Collectors.toList());
    }


    //Doanh Thu
    @Override
    public List<DailyRevenueResponse> getDailyRevenue() {
        DailyRevenueProjection projection = orderRepository.getTodayRevenue(); // gọi truy vấn CURDATE()
        if (projection == null) {
            return Collections.emptyList(); // không có đơn hàng nào hôm nay
        }
        DailyRevenueResponse response = new DailyRevenueResponse(projection.getDay(), projection.getTotalRevenue());
        return Collections.singletonList(response); // trả về danh sách chứa 1 phần tử
    }

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenue() {
        MonthlyRevenueProjection projection = orderRepository.getCurrentMonthRevenue();
        if (projection == null) {
            return Collections.emptyList(); // không có dữ liệu tháng này
        }
        YearMonth month = YearMonth.parse(projection.getMonth()); // "yyyy-MM"
        MonthlyRevenueResponse response = new MonthlyRevenueResponse(month, projection.getTotalRevenue());
        return Collections.singletonList(response);
    }

    @Override
    public List<YearlyRevenueResponse> getYearlyRevenue() {
        YearlyRevenueProjection projection = orderRepository.getCurrentYearRevenue();
        if (projection == null) {
            return Collections.emptyList();
        }

        YearlyRevenueResponse response = new YearlyRevenueResponse(
                Year.parse(projection.getYear()), // hoặc Year.of(...) nếu là int
                projection.getTotalRevenue()
        );
        return Collections.singletonList(response); // ✅ Trả về List<YearlyRevenueResponse>
    }


    //Đơn Hàng huy ngay
    @Override
    public OrderStatusTodayResponse getCancelledOrdersToday() {
        long count = orderRepository.countCancelledOrdersToday();

        return new OrderStatusTodayResponse(LocalDate.now(), count);
    }
    //Đơn Hàng huy thang
    @Override
    public OrderStatusMonthResponse getCancelledOrdersThisMonth() {
        long count = orderRepository.countCancelledOrdersThisMonth();
        LocalDate now = LocalDate.now();
        return new OrderStatusMonthResponse(now.getMonthValue(), now.getYear(), count);
    }
    //Đơn Hàng huy nam
    @Override
    public OrderStatusYearResponse getCancelledOrdersThisYear() {
        long count = orderRepository.countCancelledOrdersThisYear();
        return new OrderStatusYearResponse(LocalDate.now().getYear(), count);
    }

    //
    //Đơn Hàng hoan thanh ngay
    @Override
    public OrderStatusTodayResponse countCompletedOrdersToday() {
        long count = orderRepository.countCompletedOrdersToday();
        return new OrderStatusTodayResponse(LocalDate.now(), count);
    }
    //Đơn Hàng hoan thanh thang
    @Override
    public OrderStatusMonthResponse countCompletedOrdersThisMonth() {
        long count = orderRepository.countCompletedOrdersThisMonth();
        LocalDate now = LocalDate.now();
        return new OrderStatusMonthResponse(now.getMonthValue(), now.getYear(), count);
    }
    //Đơn Hàng hoan thanh nam
    @Override
    public OrderStatusYearResponse countCompletedOrdersThisYear() {
        long count = orderRepository.countCompletedOrdersThisYear();
        return new OrderStatusYearResponse(LocalDate.now().getYear(), count);
    }



    //Đơn Hàng huy ngay
    @Override
    public OrderStatusTodayResponse getReturnedOrdersToday() {
        long count = orderRepository.countReturnedOrdersToday();
        return new OrderStatusTodayResponse(LocalDate.now(), count);
    }
    //Đơn Hàng huy thang
    @Override
    public OrderStatusMonthResponse getReturnedOrdersThisMonth() {
        long count = orderRepository.countReturnedOrdersThisMonth();
        LocalDate now = LocalDate.now();
        return new OrderStatusMonthResponse(now.getMonthValue(), now.getYear(), count);
    }
    //Đơn Hàng huy nam
    @Override
    public OrderStatusYearResponse getReturnedOrdersThisYear() {
        long count = orderRepository.countReturnedOrdersThisYear();
        return new OrderStatusYearResponse(LocalDate.now().getYear(), count);
    }
    //theo ngày
    @Override
    public CustomStatisticalResponse getStatisticsBetween(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startDateTime = fromDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX); // 23:59:59.999

        Double revenue = orderRepository.getRevenueBetweenDates(startDateTime, endDateTime);
        Long cancelled = orderRepository.countCancelledOrdersBetweenDates(startDateTime, endDateTime);
        Long completed = orderRepository.countCompletedOrdersBetweenDates(startDateTime, endDateTime);
        Long returned = orderRepository.countReturnedOrdersBetweenDates(startDateTime, endDateTime);
        Long totalSoldQuantity = orderItemRepository.getTotalSoldQuantityBetween(startDateTime, endDateTime); // ✅ mới thêm

        return CustomStatisticalResponse.builder()
                .totalRevenue(revenue != null ? revenue : 0.0)
                .totalSoldQuantity(totalSoldQuantity != null ? totalSoldQuantity : 0L)
                .completedOrders(completed != null ? completed : 0L)
                .cancelledOrders(cancelled != null ? cancelled : 0L)
                .returnedOrders(returned != null ? returned : 0L)
                .build();
    }



    @Override
    @Transactional
    public OrderResponse findById(Long id){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ko tìm thây"));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse createInvoice(CreateInvoiceRequest request) {
        try {
            if (request == null || request.getIsPos() == null) {
                throw new IllegalArgumentException("Request hoặc trường isPos không được để trống");
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AccessDeniedException("Người dùng chưa được xác thực");
            }

            String username;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                username = (String) principal; // Xử lý khi principal là username
            } else {
                throw new AccessDeniedException("Không thể xác định thông tin người dùng");
            }

            if (!authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ADMIN"))) {
                throw new AccessDeniedException("Chỉ ADMIN mới có thể tạo đơn hàng");
            }

//            // Lấy user từ database
//            User currentUser = userRepository.findByUsername(username)
//                    .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại: " + username));

            // Tạo và lưu order
            Order order = new Order();
            order.setOrderCode(generateOrderCode());
            order.setOrderTotal(0.0);
            order.setOrderStatus(OrderStatus.PENDING);
//            order.setCreatedDate(LocalDateTime.now());
            order.setIsPos(request.getIsPos());
            order.setOrderSource(OrderSource.COUNTER);
            order.setDeleted(false);
//            order.setUser(currentUser); // Gán user
            orderRepository.save(order);

            return mapToOrderResponse(order);
        } catch (AccessDeniedException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lỗi quyền truy cập: " + e.getMessage(), e);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public OrderResponse addProductToOrder(OrderRequest request) {
        Order order = orderRepository.findByOrderCode(request.getOrderCode())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + request.getOrderCode()));

        if (!order.getOrderCode().equals(request.getOrderCode())) {
            throw new IllegalArgumentException("Mã đơn hàng không khớp");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp danh sách sản phẩm");
        }
        if (request.getPayment() == null) {
            throw new IllegalArgumentException("Vui lòng cung cấp thông tin thanh toán");
        }

        if (!order.getIsPos()) {
            if (request.getUserId() == null || request.getShipments() == null || request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp userId và thông tin shipment cho đơn giao hàng");
            }
        } else {
            if (request.getShipments() != null && !request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Đơn POS không được chứa shipment");
            }
        }

        // userId
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
            log.info("Khách hàng đã đăng ký: {}", user.getId());
        } else if (order.getIsPos()) {
            log.info("Khách vãng lai cho đơn POS");
        } else {
            throw new IllegalArgumentException("Đơn ship yêu cầu userId");
        }
        order.setUser(user);

        // Logic thêm sản phẩm
        double totalAmount = 0.0;
        double totalShippingCost = 0.0;
        List<OrderItem> savedOrderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng tồn kho");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            log.info("Số tiền của sản phẩm {}: {}", product.getName(), product.getPrice());
            OrderItem savedItem = orderItemRepository.save(orderItem);

            savedOrderItems.add(savedItem);
            totalAmount += savedItem.getQuantity() * savedItem.getUnitPrice();

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Xử lý phí vận chuyển cho đơn giao hàng
        if (!order.getIsPos()) {
            List<OrderRequest.ShipmentRequest> shipmentRequests = request.getShipments();
            if (shipmentRequests == null || shipmentRequests.isEmpty()) {
                throw new IllegalArgumentException("Danh sách shipment không được để trống đối với đơn giao hàng.");
            }

            for (OrderRequest.ShipmentRequest shipmentReq : shipmentRequests) {
                if (shipmentReq.getShippingCost() == null || shipmentReq.getShippingCost() < 0) {
                    throw new IllegalArgumentException("Phí vận chuyển không hợp lệ cho shipment với carrier ID: " + shipmentReq.getCarrierId());
                }
                totalShippingCost += shipmentReq.getShippingCost();
                log.info("Phí vận chuyển cho shipment với carrier ID {}: {}", shipmentReq.getCarrierId(), shipmentReq.getShippingCost());
            }
        }

        // Gán tổng tiền của đơn hàng (bao gồm phí vận chuyển)
        order.setOrderTotal(totalAmount + totalShippingCost);
        log.info("Tổng số tiền trước khi áp dụng coupon (bao gồm phí vận chuyển): {}", order.getOrderTotal());

        // Apply coupon if provided
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại");
            }
            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                if (user == null || !usage.getUser().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này");
                }

                Coupon coupon = usage.getCoupon();
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn");
                }

                totalDiscount += coupon.getDiscountAmount();
                log.info("Áp dụng mã giảm giá: {}, giảm: {}", coupon.getCodeCoupon(), coupon.getDiscountAmount());
                usage.setDeleted(true);
                couponUsageRepository.save(usage);
            }

            order.setOrderTotal(order.getOrderTotal() - totalDiscount);
            if (order.getOrderTotal() < 0) {
                order.setOrderTotal(0.0);
            }
            orderRepository.save(order);
        }

        log.info("Tổng số tiền sau khi áp dụng coupon: {}", order.getOrderTotal());

        // Thanh toán
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
        Payment payment = paymentOptional.orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setOrder(order);
            return newPayment;
        });
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
        payment.setPaymentMethod(paymentMethod);
        double paidAmount = request.getPayment().getAmount();
        log.info("Số tiền khách đưa: {}", paidAmount);
        payment.setAmount(paidAmount);
        payment.setChangeAmount(paidAmount - order.getOrderTotal());
        payment.setPaymentDate(LocalDateTime.now());

        // Set trạng thái thanh toán và deleted dựa trên loại đơn hàng
        if (order.getIsPos()) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            order.setOrderStatus(OrderStatus.COMPLETED);
            order.setDeleted(true);
        } else {
            payment.setPaymentStatus(PaymentStatus.PENDING);
            order.setOrderStatus(OrderStatus.PENDING);
            order.setDeleted(false);
        }
        paymentRepository.save(payment);

        // Xử lý vận chuyển cho đơn giao hàng
        if (!order.getIsPos()) {
            List<OrderRequest.ShipmentRequest> shipmentRequests = request.getShipments();
            for (OrderRequest.ShipmentRequest shipmentReq : shipmentRequests) {
                Shipment shipment = new Shipment();
                shipment.setOrder(order);
                Carrier carrier = carrierRepository.findById(shipmentReq.getCarrierId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển với ID: " + shipmentReq.getCarrierId()));
                shipment.setCarrier(carrier);
                shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
                shipment.setShipmentStatus(ShipmentStatus.PENDING);
                shipment.setTrackingNumber(generateTrackingNumber());
                shipment.setShipmentDate(new Date());
                shipment.setShippingCost(shipmentReq.getShippingCost());
                shipment.setDeleted(false);
                Shipment savedShipment = shipmentRepository.save(shipment);

                for (OrderItem item : savedOrderItems) {
                    ShipmentItem shipmentItem = new ShipmentItem();
                    shipmentItem.setShipment(savedShipment);
                    shipmentItem.setOrderItem(item);
                    shipmentItemRepository.save(shipmentItem);
                }
            }
        }

        order.setNodes(request.getNodes());
        order.setOrderDate(new Date());
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
        // 4. Validate POS and shipping logic
        if (!order.getIsPos()) {
            if (request.getUserId() == null ||  request.getShipments() == null || request.getShipments().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng cung cấp userId, addressId và shipment cho đơn giao hàng");
            }
        } else {
            if ((request.getShipments() != null && !request.getShipments().isEmpty())) {
                throw new IllegalArgumentException("Đơn POS không được chứa addressId hoặc shipment");
            }
        }

        // userId
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
            log.info("Khách hàng đã đăng ký: {}", user.getId());
        } else if (order.getIsPos()) {
            log.info("Khách vãng lai cho đơn POS");
        } else {
            throw new IllegalArgumentException("Đơn ship yêu cầu userId");
        }
        order.setUser(user);

        // 6. Process OrderItem and calculate total
        double totalAmount = 0.0;
        List<OrderItem> savedOrderItems = new ArrayList<>(); // Danh sách để tạo shipment item

        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            log.info("số tiền của sản phẩm" + product.getPrice());
            OrderItem savedItem = orderItemRepository.save(orderItem); // lưu và giữ lại

            savedOrderItems.add(savedItem); // thêm vào danh sách để dùng cho ShipmentItem

            totalAmount += savedItem.getQuantity() * savedItem.getUnitPrice();


            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Gán danh sách và tổng tiền của đơn hàng đó
        order.setOrderTotal(totalAmount);

        // 7. Apply coupon if provided
//        if (request.getCouponId() != null) {
//            Coupon coupon = couponRepository.findById(request.getCouponId())
//                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));
//            order.setOrderTotal(order.getOrderTotal() - coupon.getDiscountAmount());
//            // tổng số tiền đơn hàng đó sau khi trừ đi phiếu giảm giá còn lại
//            // đơn hàng lúc đầu 300 thì sẽ còn lại là 100
//            orderRepository.save(order);
//            if (order.getUser() != null) {
//                CouponUsage usage = new CouponUsage();
//                usage.setCoupon(coupon);
//                usage.setUser(order.getUser());
//                couponUsageRepository.save(usage);
//            }
//        }
//
//        log.info("tổng số tiền khi áp coupun vào là bao nhieu" + order.getOrderTotal());

        // 8. Handle payment
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
        Payment payment = paymentOptional.orElseGet(() -> {
            Payment newPayment = new Payment();
            newPayment.setOrder(order);
            return newPayment;
        });
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(request.getPayment().getAmount());  // tiền khách đưa 1500
        payment.setChangeAmount(payment.getAmount() - order.getOrderTotal() ); // số tiền còn lại : 1400
        boolean isCod = paymentMethod.getName().equalsIgnoreCase("COD");
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        //payment.setPaymentDate(request.getIsPos() || !isCod ? new Date() : null);

        paymentRepository.save(payment);

        // 9. Lựa chọn đơn vị giao hàng
        if (!order.getIsPos()) {
            // Process shipments
            OrderRequest.ShipmentRequest shipmentReq = request.getShipments().get(0); // Assuming single shipment for simplicity
            Shipment shipment = new Shipment();
            shipment.setOrder(order);
//            shipment.setCarrier(shipmentReq.getCarrier());
            shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
            shipment.setShipmentStatus(ShipmentStatus.PENDING);
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment.setShipmentDate(new Date());
            shipmentRepository.save(shipment);

            // Link shipment with order items
            for (OrderItem item : savedOrderItems) {
                ShipmentItem shipmentItem = new ShipmentItem();
                shipmentItem.setShipment(shipment);
                shipmentItem.setOrderItem(item);
                shipmentItemRepository.save(shipmentItem);
            }
        }
        orderRepository.save(order);
        //  orderItemRepository.saveAll(orderItemList);

        // 16. Trả về response
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getAll() {
        List<Order> orders = orderRepository.findAllOrderIsPos();
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }


    public OrderResponse mapToOrderResponse(Order order) {
    if (order == null) {
        throw new IllegalArgumentException("Order cannot be null.");
    }

    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setOrderCode(order.getOrderCode());
    //response.setUserId(order.getUser().getId()); response.setUserId(order.getUser() != null ? order.getUser().getId() : null); //
//    response.setUserName(order.getUser().getUsername());

    response.setOrderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null);
    response.setOrderTotal(order.getOrderTotal());
    response.setIsPos(order.getIsPos());
    response.setDeleted(order.getDeleted());
    response.setNodes(order.getNodes());
    response.setOrderDate(order.getOrderDate());
    response.setCreatedBy(order.getCreatedBy());
    response.setCreatedDate(order.getCreatedDate());
    response.setLastModifiedBy(order.getLastModifiedBy());
    response.setLastModifiedDate(order.getLastModifiedDate());


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
        paymentResponse.setChangeAmount(payment.getChangeAmount());
        paymentResponse.setPaymentMethodName(payment.getPaymentMethod().getName());
        paymentResponse.setPaymentMethodId(payment.getPaymentMethod().getId());
        response.setPayment(paymentResponse);
    }

    if (order.getUser() != null) {
        List<CouponUsage> couponUsages = couponUsageRepository.findByUserId(order.getUser().getId());
        response.setCouponUsages(couponUsages.stream()
                .map(couponUsage -> new OrderResponse.CouponResponse(
                        couponUsage.getId(),
                        couponUsage.getCoupon().getCodeCoupon().toString(),
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
    if (order.getIsPos() != null && !order.getIsPos()
            && order.getUser() != null && order.getUser().getId() != null) {

        Optional<UserAddressMapping> addressMappingOptional = userAddressMappingRepository
                .findByUserId(order.getUser().getId())
                .stream()
                .findFirst(); // Giả sử lấy địa chỉ đầu tiên

        if (addressMappingOptional.isPresent() && addressMappingOptional.get().getAddress() != null) {
            Address address = addressMappingOptional.get().getAddress();
            User user = addressMappingOptional.get().getUser(); // Lấy user từ mapping

            response.setAddress(new OrderResponse.AddressResponse(
                    address.getId(),
                    user.getEmail(),
                    user.getId(),
                    user.getUsername(),
                    user.getPhoneNumber(),
                    user.getRole().toString(),
                    address.getStreet(),
                    address.getWard(),
                    address.getCity(),
                    address.getState(),
                    address.getCountry(),
                    address.getZipcode(),
                    address.getDistrict(),
                    address.getProvince(),
                    user.getIsActive()
            ));
        }

    }
        // cái này đang là 1
//        Optional<Shipment> shipmentOptional = shipmentRepository.findByOrderId(order.getId());
//        if (shipmentOptional.isPresent()) {
//            Shipment shipment = shipmentOptional.get();
//            response.setShipments(new OrderResponse.ShipmentResponse(
//                    shipment.getId(),
//                    shipment.getShipmentDate(),
//                    shipment.getShipmentStatus(),
//                    shipment.getTrackingNumber(),
//                    shipment.getCarrier().getName(),
//                    shipment.getEstimatedDeliveryDate()
//            ));
//        }


        response.setShipments(shipmentRepository.findByOrderId(order.getId()).stream()
                .map(shipment -> {
                    OrderResponse.ShipmentResponse shipmentResponse = new OrderResponse.ShipmentResponse();
                    shipmentResponse.setId(shipment.getId());
                    shipmentResponse.setShipmentDate(shipment.getShipmentDate());
                    shipmentResponse.setTrackingNumber(shipment.getTrackingNumber());
                    shipmentResponse.setCarrierName(shipment.getCarrier().getName());
                    shipmentResponse.setShipmentStatus(String.valueOf(shipment.getShipmentStatus()));
                    shipmentResponse.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
                    shipmentResponse.setShippingCost(shipment.getShippingCost());
                    shipmentResponse.setCarrierId(shipment.getCarrier().getId());
                    return shipmentResponse;
                }).collect(Collectors.toList()));

        response.setItems(orderItemRepository.findByOrderId(order.getId()).stream()
            .map(orderItem -> {
                OrderItemResponse orderItemResponse = new OrderItemResponse();
                orderItemResponse.setId(orderItem.getId());
                orderItemResponse.setProductId(orderItem.getProduct().getId());
                orderItemResponse.setProductName(orderItem.getProduct().getName());
                orderItemResponse.setQuantity(orderItem.getQuantity());
                orderItemResponse.setUnitPrice(orderItem.getUnitPrice());
                return  orderItemResponse;
            }).collect(Collectors.toList()));

    return response;


        //        if (order.getIsPos() != null && !order.getIsPos()
//                && order.getUser() != null && order.getUser().getId() != null){
//            response.setAddress((OrderResponse.AddressResponse) userAddressMappingRepository.findByUserId(order.getUser().getId()).stream()
//                    .map(userAddressMapping -> {
//                                OrderResponse.AddressResponse addressResponse = new OrderResponse.AddressResponse();
//                                addressResponse.setId(userAddressMapping.getId());
//                                addressResponse.setEmail(userAddressMapping.getUser().getUsername());
//                                addressResponse.setPhoneNumber(userAddressMapping.getUser().getPhoneNumber());
//                                addressResponse.setRole(userAddressMapping.getUser().getRole().toString());
//                                addressResponse.setAddressStreet(userAddressMapping.getAddress().getStreet());
//                                addressResponse.setAddressWard(userAddressMapping.getAddress().getWard());
//                                addressResponse.setAddressCity(userAddressMapping.getAddress().getCity());
//                                addressResponse.setAddressState(userAddressMapping.getAddress().getState());
//                                addressResponse.setAddressCountry(userAddressMapping.getAddress().getCountry());
//                                addressResponse.setAddressZipcode(userAddressMapping.getAddress().getZipcode());
//                                addressResponse.setAddressDistrict(userAddressMapping.getAddress().getDistrict());
//                                addressResponse.setAddressProvince(userAddressMapping.getAddress().getProvince());
//                                //addressResponse.isActive(userAddressMapping.getUser().getIsActive());
//                                return addressResponse;
//                            }
//                            ).collect(Collectors.toList()));
//
}

    private String generateOrderCode() {
        return "NAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis();
    }

//    @Override
//    @Transactional
//    public OrderResponse addProductToOrder(OrderRequest request) {
//        // 1. Find order by code
//        Order order = orderRepository.findByOrderCode(request.getOrderCode())
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + request.getOrderCode()));
//
//        // 2. Validate required fields
//        if (request.getItems() == null || request.getItems().isEmpty()) {
//            throw new IllegalArgumentException("Vui lòng cung cấp danh sách sản phẩm");
//        }
//        if (request.getPayment() == null) {
//            throw new IllegalArgumentException("Vui lòng cung cấp thông tin thanh toán");
//        }
//
//        // 3. Validate POS and shipping logic
//        if (!request.getIsPos()) {
//            if (request.getUserId() == null || request.getAddressId() == null ||
//                    request.getShipments() == null || request.getShipments().isEmpty()) {
//                throw new IllegalArgumentException("Vui lòng cung cấp userId, addressId và shipment cho đơn giao hàng");
//            }
//            if (request.getShipments().size() != 1) {
//                throw new IllegalArgumentException("Đơn hàng chỉ được có một shipment");
//            }
//        } else {
//            if (request.getAddressId() != null || (request.getShipments() != null && !request.getShipments().isEmpty())) {
//                throw new IllegalArgumentException("Đơn POS không được chứa addressId hoặc shipment");
//            }
//        }
//
//        // 4. Process user
//        User user = null;
//        if (request.getUserId() != null) {
//            user = userRepository.findById(request.getUserId())
//                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
//            log.info("Khách hàng đã đăng ký: {}", user.getId());
//        } else if (request.getIsPos()) {
//            log.info("Khách vãng lai cho đơn POS");
//        } else {
//            throw new IllegalArgumentException("Đơn ship yêu cầu userId");
//        }
//        order.setUser(user);
//
//        // 5. Process OrderItem and calculate total
//        double totalAmount = 0.0;
//        List<OrderItem> orderItems = new ArrayList<>();
//        List<Product> updatedProducts = new ArrayList<>();
//
//        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {
//            Product product = productRepository.findById(itemRequest.getProductId())
//                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));
//            if (product.getStockQuantity() < itemRequest.getQuantity()) {
//                throw new IllegalArgumentException("Sản phẩm không đủ tồn kho: " + product.getId());
//            }
//            if (product.getPrice() == null) {
//                throw new IllegalArgumentException("Giá sản phẩm không hợp lệ: " + product.getId());
//            }
//
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrder(order);
//            orderItem.setProduct(product);
//            orderItem.setQuantity(itemRequest.getQuantity());
//            orderItem.setUnitPrice(product.getPrice());
//            log.info("Gán unitPrice cho OrderItem: productId={}, unitPrice={}", product.getId(), product.getPrice());
//            orderItems.add(orderItem);
//
//            double itemTotal = itemRequest.getQuantity() * product.getPrice();
//            totalAmount += itemTotal;
//            log.info("Sản phẩm ID: {}, Giá: {}, Số lượng: {}, Tổng tiền sản phẩm: {}",
//                    itemRequest.getProductId(), product.getPrice(), itemRequest.getQuantity(), itemTotal);
//
//            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
//            updatedProducts.add(product);
//        }
//        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
//        productRepository.saveAll(updatedProducts);
//        order.setOrderTotal(totalAmount);
//        double originalOrderTotal = totalAmount; // Lưu tổng tiền ban đầu
//        log.info("Tổng tiền đơn hàng ban đầu: {}", order.getOrderTotal());
//
//        // 6. Apply coupon if provided
//        double finalOrderTotal = order.getOrderTotal();
//        if (request.getCouponId() != null) {
//            Coupon coupon = couponRepository.findById(request.getCouponId())
//                    .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không hợp lệ hoặc đã hết hạn"));
//            double newTotal = order.getOrderTotal() - coupon.getDiscountAmount();
//            if (newTotal < 0) {
//                throw new IllegalArgumentException("Tổng tiền không thể âm sau khi áp dụng mã giảm giá");
//            }
//            order.setOrderTotal(newTotal);
//            finalOrderTotal = newTotal;
//            if (order.getUser() != null) {
//                CouponUsage usage = new CouponUsage();
//                usage.setCoupon(coupon);
//                usage.setUser(order.getUser());
//                couponUsageRepository.save(usage);
//            }
//        }
//        log.info("Tổng tiền sau khi áp coupon: {}", finalOrderTotal);
//
//        // 7. Handle payment
//        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
//        Payment payment = paymentOptional.orElseGet(() -> {
//            Payment newPayment = new Payment();
//            newPayment.setOrder(order);
//            return newPayment;
//        });
//        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
//                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ"));
//        payment.setPaymentMethod(paymentMethod);
//        payment.setAmount(request.getPayment().getAmount());
//        if (request.getPayment().getAmount() < finalOrderTotal) {
//            throw new IllegalArgumentException("Số tiền thanh toán không đủ để thanh toán đơn hàng");
//        }
//        payment.setChangeAmount(request.getPayment().getAmount() - finalOrderTotal);
//        log.info("Số tiền khách đưa: {}, Tiền thừa: {}", payment.getAmount(), payment.getChangeAmount());
//        boolean isCod = paymentMethod.getName().equalsIgnoreCase("COD");
//        payment.setPaymentStatus(request.getIsPos() || !isCod ? PaymentStatus.COMPLETED : PaymentStatus.PENDING);
//        payment.setPaymentDate(request.getIsPos() || !isCod ? new Date() : null);
//        paymentRepository.save(payment);
//
//        // 8. Handle address and shipment for shipping order
//        if (!request.getIsPos()) {
//            Address address = addressRepository.findById(request.getAddressId())
//                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy địa chỉ"));
//            boolean exists = userAddressMappingRepository
//                    .existsByUserIdAndAddressId(request.getUserId(), request.getAddressId());
//            if (!exists) {
//                UserAddressMapping mapping = new UserAddressMapping();
//                mapping.setUser(user);
//                mapping.setAddress(address);
//                userAddressMappingRepository.save(mapping);
//                log.info("Tạo mapping địa chỉ cho user");
//            }
//
//            OrderRequest.ShipmentRequest shipmentReq = request.getShipments().get(0);
//            Shipment shipment = new Shipment();
//            shipment.setOrder(order);
//            shipment.setCarrier(shipmentReq.getCarrier());
//            shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
//            shipment.setShipmentStatus(ShipmentStatus.PENDING);
//            shipment.setTrackingNumber(generateTrackingNumber());
//            shipmentRepository.save(shipment);
//
//            List<ShipmentItem> shipmentItems = savedOrderItems.stream().map(item -> {
//                ShipmentItem shipmentItem = new ShipmentItem();
//                shipmentItem.setShipment(shipment);
//                shipmentItem.setOrderItem(item);
//                return shipmentItem;
//            }).collect(Collectors.toList());
//            shipmentItemRepository.saveAll(shipmentItems);
//        }
//
//        // 9. Save order
//        orderRepository.save(order);
//
//        // 10. Return response
//        OrderResponse response = mapToOrderResponse(order, originalOrderTotal); // Truyền originalOrderTotal
//        log.info("OrderResponse: orderTotal={}, originalOrderTotal={}, items={}",
//                response.getOrderTotal(), response.getOriginalOrderTotal(), response.getItems());
//        return response;
//    }

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
