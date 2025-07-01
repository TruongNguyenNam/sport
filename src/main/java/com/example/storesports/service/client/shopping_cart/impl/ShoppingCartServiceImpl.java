package com.example.storesports.service.client.shopping_cart.impl;

import com.example.storesports.core.admin.order.payload.OrderResponse;
import com.example.storesports.core.admin.orderItem.payload.OrderItemResponse;
import com.example.storesports.core.admin.payment.payload.PaymentResponse;
import com.example.storesports.core.client.shopping_cart.payload.OrderRequestClient;
import com.example.storesports.core.client.shopping_cart.payload.OrderResponseClient;
import com.example.storesports.core.client.shopping_cart.payload.ShoppingCartRequest;
import com.example.storesports.core.client.shopping_cart.payload.ShoppingCartResponse;
import com.example.storesports.core.client.wishlist.payload.WishlistResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.constant.CouponStatus;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.infrastructure.constant.PaymentStatus;
import com.example.storesports.infrastructure.constant.ShipmentStatus;
import com.example.storesports.repositories.*;
import com.example.storesports.service.client.shopping_cart.ShoppingCartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartItemRepository shoppingCartItemRepository;

    private final UserRepository userRepository;

    private final ProductRepository productRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductTagMappingRepository productTagMappingRepository;

    private final ProductAttributeValueRepository productAttributeValueRepository;


    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository;


    private final PaymentRepository paymentRepository;


    private final CouponRepository couponRepository;

    private final CouponUsageRepository couponUsageRepository;

    private final UserAddressMappingRepository userAddressMappingRepository;

    private final AddressRepository addressRepository;

    private final ShipmentRepository shipmentRepository;

    private final PaymentMethodRepository paymentMethodRepository;

    private final ShipmentItemRepository shipmentItemRepository;

    private final CarrierRepository carrierRepository;

    @Override
    public ShoppingCartResponse addToCart(ShoppingCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không có sản phẩm này"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không thấy user này"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Sản phẩm không đủ hàng trong kho");
        }

        Optional<ShoppingCartItem> existingItemOpt = shoppingCartItemRepository
                .findByUserIdAndProductId(user.getId(), product.getId());

        ShoppingCartItem item;

        if (existingItemOpt.isPresent()) {
            item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            item.setQuantity(newQuantity);

            item.setTotalPrice(product.getPrice() * newQuantity);
        } else {
            // Chưa có item → thêm mới
            item = new ShoppingCartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            item.setTotalPrice(product.getPrice() * request.getQuantity());
            item.setDeleted(false);
        }
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        ShoppingCartItem savedItem = shoppingCartItemRepository.save(item);

        return mapToShoppingCartResponse(savedItem);
    }



    @Override
    public List<ShoppingCartResponse> viewToCart(Long userId) {
        List<ShoppingCartItem> shoppingCartItems = shoppingCartItemRepository.findByUserId(userId);
        if(shoppingCartItems.isEmpty()){
            throw new IllegalArgumentException("giỏ hàng của khách hàng chưa có sản phẩm");
        }
        return shoppingCartItems.stream().map(this::mapToShoppingCartResponse).collect(Collectors.toList());
    }


    @Override
    public void removeProductWithCart(Long id) {
        ShoppingCartItem cart = shoppingCartItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("không tìm thấy có trog giỏ hàng: " + id));
        Product product = cart.getProduct();
        product.setStockQuantity(product.getStockQuantity() + cart.getQuantity());
        productRepository.save(product);
        shoppingCartItemRepository.delete(cart);
    }
    @Override
    public ShoppingCartResponse updateCartQuantity(Long id, Integer newQuantity) {
        ShoppingCartItem shoppingCartItem = shoppingCartItemRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("đã tìm thấy được giỏ hàng")
        );
        Product product = shoppingCartItem.getProduct();
        if (product.getStockQuantity() + shoppingCartItem.getQuantity() < newQuantity) {
            throw new IllegalArgumentException("không đủ số lượng sản phẩm.");
        }
        shoppingCartItem.setQuantity(newQuantity);
        ShoppingCartItem updatedCart = shoppingCartItemRepository.save(shoppingCartItem);
        product.setStockQuantity(product.getStockQuantity() + shoppingCartItem.getQuantity() - newQuantity);
        productRepository.save(product);

        return mapToShoppingCartResponse(updatedCart);

    }

    @Override
    public long countCartItemsByUserId(Long userId) {
        return shoppingCartItemRepository.countByUserIdAndDeletedFalse(userId);
    }



    private ShoppingCartResponse mapToShoppingCartResponse(ShoppingCartItem shoppingCartItem) {
        ShoppingCartResponse response = new ShoppingCartResponse();
        response.setId(shoppingCartItem.getId());
        if (shoppingCartItem.getUser() != null) {
            response.setUserName(shoppingCartItem.getUser().getUsername());
        }

        response.setQuantity(shoppingCartItem.getQuantity());
        response.setTotalPrice(shoppingCartItem.getTotalPrice());
        if(shoppingCartItem.getProduct() != null){
            ShoppingCartResponse.Product product = mapToCartProductResponse(shoppingCartItem.getProduct());
            response.setProduct(product);
        }
        response.setDeleted(shoppingCartItem.getDeleted());
        return response;
    }

    private ShoppingCartResponse.Product mapToCartProductResponse(Product product) {
        ShoppingCartResponse.Product response = new ShoppingCartResponse.Product();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setParentProductId(product.getParentProductId());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSportType(product.getSportType());
        response.setSku(product.getSku());

        if (product.getSupplier() != null) {
            response.setSupplierName(product.getSupplier().getName());
        }
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }

        // Ánh xạ tagName
        response.setTagName(productTagMappingRepository.findByProductId(product.getId())
                .stream()
                .map(productTagMapping -> productTagMapping.getTag().getName())
                .collect(Collectors.toList()));

        // Ánh xạ imageUrl
        response.setImageUrl(productImageRepository.findByProductId(product.getId())
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList()));

        // Ánh xạ productAttributeValueResponses
        response.setProductAttributeValueResponses(
                productAttributeValueRepository.findByProductId(product.getId())
                        .stream()
                        .map(productAttributeValue -> {
                            ShoppingCartResponse.Product.ProductAttributeValueResponse optionResponse =
                                    new ShoppingCartResponse.Product.ProductAttributeValueResponse();
                            optionResponse.setId(productAttributeValue.getId());
                            optionResponse.setAttributeId(productAttributeValue.getAttribute().getId());
                            optionResponse.setAttributeName(productAttributeValue.getAttribute().getName());
                            optionResponse.setProductId(productAttributeValue.getProduct().getId());
                            optionResponse.setValue(productAttributeValue.getValue());
                            return optionResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }


    @Transactional
    @Override
    public OrderResponseClient checkout(OrderRequestClient request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID must coincide with Shopping Cart.");
        }

        // 2. Kiểm tra giỏ hàng
        List<ShoppingCartItem> cartItems = shoppingCartItemRepository.findByUserId(request.getUserId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống, không thể thanh toán.");
        }

        // 3. Tạo items từ giỏ hàng nếu request.items rỗng
        if (request.getItems() == null || request.getItems().isEmpty()) {
            request.setItems(cartItems.stream()
                    .map(cartItem -> new OrderRequestClient.OrderItemRequest(cartItem.getProduct().getId(), cartItem.getQuantity()))
                    .collect(Collectors.toList()));
        }

        // 4. Kiểm tra payment
        if (request.getPayment() == null) {
            throw new IllegalArgumentException("Vui lòng cung cấp thông tin thanh toán.");
        }

        // 5. Kiểm tra shipments
        if (request.getShipments() == null || request.getShipments().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng cung cấp thông tin vận chuyển.");
        }

        // 6. Kiểm tra user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
        log.info("Khách hàng: {}", user.getId());

        // 7. Tính tổng tiền và tạo OrderItemResponse
        double totalAmount = 0.0;
        List<OrderItemResponse> orderItems = new ArrayList<>();
        for (OrderRequestClient.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ tồn kho.");
            }
            totalAmount += product.getPrice() * itemRequest.getQuantity();
            orderItems.add(new OrderItemResponse(null, product.getId(), product.getName(), itemRequest.getQuantity(), product.getPrice()));
        }

        // 8. Áp dụng mã giảm giá
        List<OrderResponseClient.CouponResponse> couponResponses = new ArrayList<>();
        if (request.getCouponUsageIds() != null && !request.getCouponUsageIds().isEmpty()) {
            List<CouponUsage> couponUsages = couponUsageRepository.findAllById(request.getCouponUsageIds());
            if (couponUsages.size() != request.getCouponUsageIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều CouponUsage không tồn tại.");
            }

            double totalDiscount = 0.0;
            for (CouponUsage usage : couponUsages) {
                if (!usage.getUser().getId().equals(request.getUserId())) {
                    throw new IllegalArgumentException("CouponUsage không thuộc về khách hàng này.");
                }
                Coupon coupon = couponRepository.findById(usage.getCoupon().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Coupon không tồn tại."));
                if (coupon.getCouponStatus() != CouponStatus.ACTIVE ||
                        coupon.getExpirationDate().isBefore(LocalDateTime.now()) ||
                        coupon.getDeleted()) {
                    throw new IllegalArgumentException("Mã giảm giá " + coupon.getCodeCoupon() + " không hợp lệ hoặc đã hết hạn.");
                }
                totalDiscount += coupon.getDiscountAmount();
                usage.setUsed(true);
                usage.setUsedDate(new Date());
                usage.setDeleted(true); // Theo yêu cầu
                usage.setLastModifiedBy(request.getUserId().intValue());
                usage.setLastModifiedDate(LocalDateTime.now());
                couponUsageRepository.save(usage);

//                couponResponses.add(new OrderResponseClient.CouponResponse(
//                        usage.getId(),
//                        coupon.getCodeCoupon(),
//                        coupon.getDiscountAmount(),
//                        coupon.getCouponUsages().get().getUsedDate()
//                        usage.getCreatedBy(),
//                        usage.getCreatedDate(),
//                        usage.getLastModifiedBy(),
//                        usage.getLastModifiedDate()
//                ));
            }
            totalAmount = Math.max(0, totalAmount - totalDiscount);
            log.info("Tổng tiền sau giảm giá: {}", totalAmount);
        }

        // 9. Kiểm tra payment amount
        if (request.getPayment().getAmount() < totalAmount) {
            throw new IllegalArgumentException("Số tiền thanh toán không đủ.");
        }

        // 10. Tạo đơn hàng
        Order order = new Order();
        order.setUser(user);
        order.setOrderCode(generateOrderCode());
        order.setOrderDate(new Date());
        order.setOrderStatus(OrderStatus.PENDING); // Ban đầu là PENDING
        order.setOrderTotal(totalAmount);
        order.setIsPos(false);
        order.setNodes(request.getNodes());
        order.setDeleted(false); // Sẽ đặt true sau khi thanh toán
        order.setCreatedBy(request.getUserId().intValue());
        order.setCreatedDate(LocalDateTime.now());
        order = orderRepository.save(order);

        // 11. Tạo chi tiết đơn hàng và cập nhật tồn kho
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderRequestClient.OrderItemRequest itemRequest = request.getItems().get(i);
            Product product = productRepository.findById(itemRequest.getProductId()).orElseThrow();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setCreatedBy(request.getUserId().intValue());
            orderItem.setCreatedDate(LocalDateTime.now());
            OrderItem savedItem = orderItemRepository.save(orderItem);
            savedOrderItems.add(savedItem);

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            orderItems.get(i).setId(savedItem.getId());
        }

        // 12. Tạo thanh toán
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ."));
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(totalAmount); // Số tiền chính xác cần thanh toán
        payment.setChangeAmount(0.0); // Không có tiền thừa trong thanh toán trực tuyến
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING); // Chờ xác nhận từ cổng thanh toán
        payment.setCreatedBy(request.getUserId().intValue());
        payment.setCreatedDate(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        // 13. Tạo vận chuyển
        OrderRequestClient.ShipmentRequest shipmentRequest = request.getShipments().get(0); // Giả định 1 shipment
        Carrier carrier = carrierRepository.findById(shipmentRequest.getCarrierId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển."));
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setCarrier(carrier);
        shipment.setShipmentDate(new Date());
        shipment.setEstimatedDeliveryDate(shipmentRequest.getEstimatedDeliveryDate());
        shipment.setShipmentStatus(ShipmentStatus.PENDING);
        shipment.setDeleted(false);
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setCreatedBy(request.getUserId().intValue());
        shipment.setCreatedDate(LocalDateTime.now());
        Shipment savedShipment = shipmentRepository.save(shipment);

        for (OrderItem item : savedOrderItems) {
            ShipmentItem shipmentItem = new ShipmentItem();
            shipmentItem.setShipment(savedShipment);
            shipmentItem.setOrderItem(item);
            shipmentItem.setCreatedBy(request.getUserId().intValue());
            shipmentItem.setCreatedDate(LocalDateTime.now());
            shipmentItemRepository.save(shipmentItem);
        }

        // 14. Cập nhật trạng thái đơn hàng và đánh dấu deleted
        order.setOrderStatus(OrderStatus.SHIPPED); // Đặt thành SHIPPED sau khi tạo shipment
        order.setDeleted(true); // Theo yêu cầu
        order.setLastModifiedBy(request.getUserId().intValue());
        order.setLastModifiedDate(LocalDateTime.now());
        orderRepository.save(order);

        // 15. Xóa giỏ hàng
        shoppingCartItemRepository.deleteAll(cartItems); //

        // 16. Trả về kết quả
        return mapToOrderResponse(order);
    }

    public OrderResponseClient mapToOrderResponse(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        OrderResponseClient response = new OrderResponseClient();
        response.setId(order.getId());
        response.setOrderCode(order.getOrderCode());
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

        // Map order items
        response.setItems(orderItemRepository.findByOrderId(order.getId()).stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getId(),
                        orderItem.getProduct().getId(),
                        orderItem.getProduct().getName(),
                        orderItem.getQuantity(),
                        orderItem.getUnitPrice()
                ))
                .collect(Collectors.toList()));


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
            response.setPayment(paymentResponse);
        }

        // Map coupon usages
        List<OrderResponseClient.CouponResponse> couponResponses = new ArrayList<>();
        if (order.getUser() != null) {
            List<CouponUsage> couponUsages = couponUsageRepository.findByUserId(order.getUser().getId());
            couponResponses = couponUsages.stream()
                    .map(couponUsage -> new OrderResponseClient.CouponResponse(
                            couponUsage.getId(),
                            couponUsage.getCoupon().getCodeCoupon(),
                            couponUsage.getCoupon().getDiscountAmount(),
                            couponUsage.getUsedDate(),
                            couponUsage.getCreatedBy(),
                            couponUsage.getCreatedDate(),
                            couponUsage.getLastModifiedBy(),
                            couponUsage.getLastModifiedDate()
                    ))
                    .collect(Collectors.toList());
        }
        response.setCouponUsages(couponResponses);

        // Map address
        OrderResponseClient.AddressResponse addressResponse = null;
        if (!order.getIsPos() && order.getUser() != null) {
            Optional<UserAddressMapping> addressMappingOptional = userAddressMappingRepository
                    .findByUserId(order.getUser().getId())
                    .stream()
                    .findFirst();
            if (addressMappingOptional.isPresent()) {
                Address address = addressMappingOptional.get().getAddress();
                User user = addressMappingOptional.get().getUser();
                addressResponse = new OrderResponseClient.AddressResponse(
                        address.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getPhoneNumber(),
                        user.getRole().name(),
                        address.getStreet(),
                        address.getWard(),
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getZipcode(),
                        address.getDistrict(),
                        address.getProvince(),
                        user.getIsActive()
                );
            }
        }
        response.setAddress(addressResponse);

        // Map shipment
        Optional<Shipment> shipmentOptional = shipmentRepository.findByOrderId(order.getId()).stream().findFirst();
        if (shipmentOptional.isPresent()) {
            Shipment shipment = shipmentOptional.get();
            response.setShipment(new OrderResponseClient.ShipmentResponse(
                    shipment.getId(),
                    shipment.getShipmentDate(),
                    shipment.getShipmentStatus().name(),
                    shipment.getTrackingNumber(),
                    shipment.getCarrier().getName(),
                    shipment.getEstimatedDeliveryDate()
            ));
        } else {
            log.warn("No shipment found for order ID: {}", order.getId());
        }

        return response;
    }

    private String generateOrderCode() {
        return "NAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis();
    }











}
