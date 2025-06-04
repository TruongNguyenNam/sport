package com.example.storesports.service.admin.order.impl;


import com.example.storesports.core.admin.order.payload.CreateInvoiceRequest;
import com.example.storesports.core.admin.order.payload.OrderRequest;
import com.example.storesports.core.admin.order.payload.OrderResponse;
import com.example.storesports.core.admin.orderItem.payload.OrderItemResponse;
import com.example.storesports.core.admin.payment.payload.PaymentResponse;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.*;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.order.OrderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderTotal(0.0);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setIsPos(request.getIsPos());
        order.setDeleted(false);
        order.setCreatedBy(1); // ADMIN sẽ là người tạo đơn hàng
        order.setCreatedDate(LocalDateTime.now());
        orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse addProductToOrder(OrderRequest request) {
        // 1. Find order by code
        Order order = orderRepository.findByOrderCode(request.getOrderCode())
                .orElseThrow(() -> new IllegalArgumentException("không tìm thấy đơn hàng với mã"));

        // 2. Validate order code
        if (!order.getOrderCode().equals(request.getOrderCode())) {
            throw new IllegalArgumentException("Mã đơn hàng không khớp");
        }

        // 3. Validate required fields
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
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại");
            }

            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                // Kiểm tra xem CouponUsage có thuộc về khách hàng không
                if (user == null || !usage.getUser().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này");
                }

                Coupon coupon = usage.getCoupon();
                // Kiểm tra tính hợp lệ của coupon
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn");
                }

                // Áp dụng giảm giá
                totalDiscount += coupon.getCouponAmount();
                log.info("Áp dụng mã giảm giá: {}, giảm: {}", coupon.getCodeCoupon(), coupon.getCouponAmount());
                usage.setDeleted(true);
                couponUsageRepository.save(usage);
            }

            // Cập nhật tổng tiền sau khi áp dụng tất cả mã giảm giá
            order.setOrderTotal(order.getOrderTotal() - totalDiscount);
            if (order.getOrderTotal() < 0) {
                order.setOrderTotal(0.0); // Đảm bảo tổng tiền không âm
            }
            orderRepository.save(order);
        }

        log.info("Tổng số tiền sau khi áp dụng coupon: {}", order.getOrderTotal());


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
        payment.setChangeAmount(paidAmount - order.getOrderTotal()); // Tính tiền thừa
        log.info("Tổng tiền cần trả: {}, Tiền thừa: {}", order.getOrderTotal(), payment.getChangeAmount()); // số tiền còn lại : 1400
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        order.setOrderStatus(OrderStatus.COMPLETED);

        // 9. Lựa chọn đơn vị giao hàng
        if (!order.getIsPos()) {
            // Process shipments
            OrderRequest.ShipmentRequest shipmentReq = request.getShipments().get(0); // Assuming single shipment for simplicity
            Shipment shipment = shipmentRepository.findById(shipmentReq.getShipmentId()).orElseThrow(() ->
                    new IllegalArgumentException("chưa tìm thấy shipmentIds"));
            shipment.setOrder(order);
//            shipment.setCarrier(shipmentReq.getCarrier());
            shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
            shipment.setShipmentStatus(ShipmentStatus.PENDING);
            shipment.setTrackingNumber(generateTrackingNumber());
            Shipment shipment1 =  shipmentRepository.save(shipment);

            // Link shipment with order items
            for (OrderItem item : savedOrderItems) {
                ShipmentItem shipmentItem = new ShipmentItem();
                shipmentItem.setShipment(shipment1);
                shipmentItem.setOrderItem(item);
                shipmentItemRepository.save(shipmentItem);
            }
            order.setOrderStatus(OrderStatus.SHIPPED);

        }

        order.setDeleted(true); // đã thanh toán thành 0
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
            shipment.setCarrier(shipmentReq.getCarrier());
            shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
            shipment.setShipmentStatus(ShipmentStatus.PENDING);
            shipment.setTrackingNumber(generateTrackingNumber());
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
        paymentResponse.setPaymentMethodName(payment.getPaymentMethod().getName());
        response.setPayment(paymentResponse);
    }

    if (order.getUser() != null) {
        List<CouponUsage> couponUsages = couponUsageRepository.findByUserId(order.getUser().getId());
        response.setCouponUsages(couponUsages.stream()
                .map(couponUsage -> new OrderResponse.CouponResponse(
                        couponUsage.getId(),
                        couponUsage.getCoupon().getCodeCoupon().toString(),
                        couponUsage.getCoupon().getCouponAmount(),
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
