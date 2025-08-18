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
import com.example.storesports.infrastructure.configuration.vnpay.VnPayConfig;
import com.example.storesports.infrastructure.constant.*;
import com.example.storesports.infrastructure.exceptions.CartEmptyException;
import com.example.storesports.repositories.*;
import com.example.storesports.service.client.shopping_cart.ShoppingCartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            throw new CartEmptyException("giỏ hàng của khách hàng chưa có sản phẩm");
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
    public OrderResponseClient checkoutv2(OrderRequestClient request) {
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
        order.setOrderSource(OrderSource.CLIENT);
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




    @Transactional
    @Override
    public OrderResponseClient checkout(OrderRequestClient request, HttpServletRequest httpServletRequest) {
        log.info("Bắt đầu xử lý thanh toán cho user: {}", request.getUserId());

        // 1. Kiểm tra HttpServletRequest
        if (httpServletRequest == null) {
            throw new IllegalArgumentException("Yêu cầu HTTP không được để trống");
        }

        // 2. Kiểm tra userId
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID là bắt buộc");
        }
        if (request.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID phải là số dương");
        }

        // 3. Kiểm tra giỏ hàng
        List<ShoppingCartItem> cartItems = shoppingCartItemRepository.findByUserId(request.getUserId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống, không thể thanh toán.");
        }

        // 4. Tạo items từ giỏ hàng nếu request.items rỗng
        if (request.getItems() == null || request.getItems().isEmpty()) {
            request.setItems(cartItems.stream()
                    .map(cartItem -> new OrderRequestClient.OrderItemRequest(cartItem.getProduct().getId(), cartItem.getQuantity()))
                    .collect(Collectors.toList()));
        }

        // 5. Kiểm tra payment
        if (request.getPayment() == null) {
            throw new IllegalArgumentException("Thông tin thanh toán là bắt buộc.");
        }

        // 6. Kiểm tra shipments
        if (request.getShipments() == null || request.getShipments().isEmpty()) {
            throw new IllegalArgumentException("Thông tin vận chuyển là bắt buộc cho đơn hàng trực tuyến.");
        }
        for (OrderRequestClient.ShipmentRequest shipment : request.getShipments()) {
            if (shipment.getCarrierId() == null) {
                throw new IllegalArgumentException("Carrier ID là bắt buộc.");
            }
            if (shipment.getCarrierId() <= 0) {
                throw new IllegalArgumentException("Carrier ID phải là số dương.");
            }
            if (shipment.getShippingCost() == null || shipment.getShippingCost() < 0) {
                throw new IllegalArgumentException("Phí vận chuyển không hợp lệ cho shipment với carrier ID: " + shipment.getCarrierId());
            }
            if (shipment.getEstimatedDeliveryDate() == null) {
                throw new IllegalArgumentException("Ngày giao hàng dự kiến là bắt buộc.");
            }
        }

        // 7. Kiểm tra user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy userId: " + request.getUserId()));
        log.info("Khách hàng: {}", user.getId());

        // 8. Tạo đơn hàng
        Order order = new Order();
        order.setIsPos(false);
        if (!order.getIsPos() && request.getAddressId() != null) {
            Optional<UserAddressMapping> addressMapping = userAddressMappingRepository
                    .findByUserIdAndAddressId(request.getUserId(), request.getAddressId());
            if (!addressMapping.isPresent()) {
                throw new IllegalArgumentException("Địa chỉ với ID " + request.getAddressId() + " không hợp lệ hoặc không thuộc về người dùng");
            }
            order.setAddressId(request.getAddressId()); // Gán addressId vào Order
        }
        order.setUser(user);
        order.setOrderCode(generateOrderCode());
        order.setOrderDate(new Date());
        order.setOrderStatus(OrderStatus.PENDING);

        order.setOrderSource(OrderSource.CLIENT);
        order.setNodes(request.getNodes());
        order.setDeleted(false);
        order.setCreatedBy(request.getUserId().intValue());
        order.setCreatedDate(LocalDateTime.now());

        // 9. Tính tổng tiền sản phẩm và tạo OrderItem
        double totalAmount = 0.0;
        double totalShippingCost = 0.0;
        List<OrderItem> savedOrderItems = new ArrayList<>();
        List<OrderResponseClient.OrderItemResponse> orderItems = new ArrayList<>();

        for (OrderRequestClient.OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Thông tin sản phẩm không hợp lệ.");
            }
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + itemRequest.getProductId()));
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ tồn kho.");
            }

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

            totalAmount += product.getPrice() * itemRequest.getQuantity();
            orderItems.add(new OrderResponseClient.OrderItemResponse(savedItem.getId(), product.getId(), product.getName(), itemRequest.getQuantity(), product.getPrice()));
        }

        // 10. Áp dụng mã giảm giá
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
                usage.setOrder(order);
                usage.setDeleted(true);
                usage.setLastModifiedBy(request.getUserId().intValue());
                usage.setLastModifiedDate(LocalDateTime.now());
                couponUsageRepository.save(usage);

                couponResponses.add(new OrderResponseClient.CouponResponse(
                        usage.getId(),
                        coupon.getCodeCoupon(),
                        coupon.getDiscountAmount(),
                        usage.getUsedDate(),
                        usage.getCreatedBy(),
                        usage.getCreatedDate(),
                        usage.getLastModifiedBy(),
                        usage.getLastModifiedDate()
                ));
            }
            totalAmount = Math.max(0, totalAmount - totalDiscount);
            log.info("Tổng tiền sau giảm giá: {}", totalAmount);
        }

        // 11. Xử lý phí vận chuyển
        for (OrderRequestClient.ShipmentRequest shipmentReq : request.getShipments()) {
            totalShippingCost += shipmentReq.getShippingCost();
            log.info("Phí vận chuyển cho shipment với carrier ID {}: {}", shipmentReq.getCarrierId(), shipmentReq.getShippingCost());
        }

        // 12. Cập nhật tổng tiền đơn hàng
        order.setOrderTotal(totalAmount);
        order = orderRepository.save(order);

        // 13. Tạo thanh toán
        if (request.getPayment().getPaymentMethodId() == null) {
            throw new IllegalArgumentException("Phương thức thanh toán là bắt buộc.");
        }
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPayment().getPaymentMethodId())
                .orElseThrow(() -> new IllegalArgumentException("Phương thức thanh toán không hợp lệ."));
        double totalAmountWithShipping = totalAmount + totalShippingCost;

        if (request.getPayment().getAmount() == null || request.getPayment().getAmount() < totalAmountWithShipping) {
            throw new IllegalArgumentException("Số tiền thanh toán không đủ: " + (request.getPayment().getAmount() != null ? request.getPayment().getAmount() : "null") + " < " + totalAmountWithShipping);
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(totalAmountWithShipping);
        payment.setChangeAmount(0.0);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setCreatedBy(request.getUserId().intValue());
        payment.setCreatedDate(LocalDateTime.now());

        String paymentUrl = null;
        if (paymentMethod.getId().equals(2L)) { // VNPay
            if (Math.abs(request.getPayment().getAmount() - totalAmountWithShipping) > 0.01) {
                throw new IllegalArgumentException("Số tiền thanh toán VNPay phải khớp với tổng tiền: " + request.getPayment().getAmount() + " != " + totalAmountWithShipping);
            }
            String vnp_TxnRef = order.getOrderCode() + "-" + VnPayConfig.getRandomNumber(8);
            paymentUrl = createVnpayPaymentUrl(order, totalAmountWithShipping, vnp_TxnRef, request.getPayment().getReturnUrl(), httpServletRequest);
            log.info("Tạo URL thanh toán VNPay: {}", paymentUrl);
            payment.setTransactionId(vnp_TxnRef);
            payment.setReturnUrl(request.getPayment().getReturnUrl());
        }

        payment = paymentRepository.save(payment);

        // 14. Tạo vận chuyển
        for (OrderRequestClient.ShipmentRequest shipmentReq : request.getShipments()) {
            Carrier carrier = carrierRepository.findById(shipmentReq.getCarrierId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn vị vận chuyển với ID: " + shipmentReq.getCarrierId()));
            Shipment shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setCarrier(carrier);
            shipment.setShipmentDate(new Date());
            shipment.setEstimatedDeliveryDate(shipmentReq.getEstimatedDeliveryDate());
            shipment.setShipmentStatus(ShipmentStatus.PENDING);
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment.setShippingCost(shipmentReq.getShippingCost());
            shipment.setDeleted(false);
            shipment.setCreatedBy(request.getUserId().intValue());
            shipment.setCreatedDate(LocalDateTime.now());
            Shipment savedShipment = shipmentRepository.save(shipment);

            // Gán order items vào shipment
            for (OrderItem item : savedOrderItems) {
                ShipmentItem shipmentItem = new ShipmentItem();
                shipmentItem.setShipment(savedShipment);
                shipmentItem.setOrderItem(item);
                shipmentItemRepository.save(shipmentItem);
            }
        }

        // 15. Xóa giỏ hàng
        shoppingCartItemRepository.deleteAll(cartItems);

        // 16. Trả về kết quả
        OrderResponseClient response = mapToOrderResponse(order);
        response.setItems(orderItems);
        response.setCouponUsages(couponResponses);
        response.setPaymentUrl(paymentUrl);
        log.info("Hoàn tất xử lý đơn hàng {}: status = {}, deleted = {}, paymentUrl = {}",
                order.getOrderCode(), order.getOrderStatus(), order.getDeleted(), paymentUrl);
        return response;
    }

    @Override
    @Transactional
    public List<OrderResponseClient> findByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByUserId(customerId);
        return orders.stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

//    @Override
//    @Transactional
//    public OrderResponseClient updateOrderStatus(String orderCode) {
//
//        // 1. Tìm đơn hàng
//        Order order = orderRepository.findByOrderCode(orderCode)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
//
//        if (order.getOrderStatus().equals(OrderStatus.PENDING)) {
//            order.setOrderStatus(OrderStatus.CANCELLED);
//            order.setLastModifiedDate(LocalDateTime.now());
////            order.setOrderTotal((double) 0);
//
//
//            // 4. Lấy danh sách OrderItem và cập nhật lại stock cho sản phẩm
//            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
//            for (OrderItem item : orderItems) {
//                List<ShipmentItem> shipmentItems = shipmentItemRepository.findByOrderItem(item);
//                if (!shipmentItems.isEmpty()) {
//
//                    shipmentItemRepository.deleteAll(shipmentItems);
//                }
//            }
//
//            for (OrderItem item : orderItems) {
//                Product product = item.getProduct();
//                product.setStockQuantity(product.getStockQuantity() + item.getQuantity()); // hoàn kho
//                productRepository.save(product);
//            }
//
//            // 5. Xoá các OrderItem khỏi DB
//            orderItemRepository.deleteAll(orderItems);
//        } else {
//            throw new IllegalArgumentException("Chỉ hỗ trợ huỷ đơn hàng (CANCELLED)");
//        }
//
//        // 6. Lưu đơn hàng
//        orderRepository.save(order);
//
//        // 7. Trả về response sau khi cập nhật
//        OrderResponseClient response = mapToOrderResponse(order);
//        response.setItems(new ArrayList<>()); // Không còn sản phẩm nào trong đơn
//        return response;
//    }
@Override
@Transactional
public OrderResponseClient updateOrderStatus(String orderCode) {

    // 1. Tìm đơn hàng
    Order order = orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

    if (order.getOrderStatus() == OrderStatus.PENDING) {
        // 2. Cập nhật trạng thái đơn
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setLastModifiedDate(LocalDateTime.now());

        // 3. Lấy danh sách OrderItem
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        // 4. Duyệt từng OrderItem để hoàn kho và cập nhật ShipmentStatus
        for (OrderItem item : orderItems) {
            // Hoàn kho
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);

            // Cập nhật trạng thái shipment
            List<ShipmentItem> shipmentItems = shipmentItemRepository.findByOrderItem(item);
            for (ShipmentItem shipmentItem : shipmentItems) {
                Shipment shipment = shipmentItem.getShipment();
                shipment.setShipmentStatus(ShipmentStatus.CANCELED);
                shipmentRepository.save(shipment);
            }
        }

    } else {
        throw new IllegalArgumentException("Chỉ hỗ trợ huỷ đơn hàng (CANCELLED)");
    }

    // 5. Lưu đơn hàng
    orderRepository.save(order);

    // 6. Trả về response
    OrderResponseClient response = mapToOrderResponse(order);
    // Giữ nguyên danh sách sản phẩm vì vẫn cần hiển thị lịch sử
    return response;
}


    @Override
    public OrderResponseClient findByOrderCode(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode).orElseThrow(() ->
                new IllegalArgumentException("không có mã đơn hàng này"));
        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponseClient updateOrderPending(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode).orElseThrow(() ->
                new IllegalArgumentException("không có mã đơn hàng này"));
        if(order.getOrderStatus() == OrderStatus.PENDING){
            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }

        }

        return null;
    }




    private String createVnpayPaymentUrl(Order order, double totalAmount, String vnp_TxnRef, String returnUrl, HttpServletRequest httpRequest) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_OrderInfo = "Thanh toan don hang " + order.getOrderCode();
            String vnp_OrderType = "billpayment";
            String vnp_IpAddr = VnPayConfig.getIpAddress(httpRequest);
            String vnp_CreateDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String vnp_ExpireDate = LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            String cleanReturnUrl = VnPayConfig.vnp_ReturnUrl;
            if (returnUrl != null && !returnUrl.trim().isEmpty() && !"null".equalsIgnoreCase(returnUrl.trim())) {
                cleanReturnUrl = returnUrl.trim();
            }
            log.info("🔁 [VNPay] returnUrl: {}", cleanReturnUrl);

            Map<String, String> vnp_Params = new TreeMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", VnPayConfig.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf((long) (totalAmount * 100)));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", cleanReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            StringBuilder queryUrl = new StringBuilder();
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                queryUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .append("&");
            }

            String secureHash = VnPayConfig.hashAllFields(vnp_Params);
            queryUrl.append("vnp_SecureHash=").append(secureHash);

            String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl.toString();
            log.info("✅ [VNPay] Tạo URL thanh toán cho đơn hàng {}: {}", order.getOrderCode(), paymentUrl);
            return paymentUrl;
        } catch (Exception e) {
            log.error("💥 [VNPay] Lỗi khi tạo URL thanh toán: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Không thể tạo URL thanh toán VNPay: " + e.getMessage());
        }
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
        List<OrderResponseClient.OrderItemResponse> itemResponses = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(orderItem -> new OrderResponseClient.OrderItemResponse(
                        orderItem.getId(),
                        orderItem.getProduct() != null ? orderItem.getProduct().getId() : null,
                        orderItem.getProduct() != null ? orderItem.getProduct().getName() : null,
                        orderItem.getQuantity(),
                        orderItem.getUnitPrice()
                ))
                .collect(Collectors.toList());

        response.setItems(itemResponses);



        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(order.getId());
        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();

            OrderResponseClient.PaymentResponse paymentResponse = new OrderResponseClient.PaymentResponse();
            paymentResponse.setId(payment.getId());
            paymentResponse.setAmount(payment.getAmount());
            paymentResponse.setPaymentStatus(payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null);
            paymentResponse.setPaymentDate(payment.getPaymentDate());
            paymentResponse.setChangeAmount(payment.getChangeAmount());

            if (payment.getPaymentMethod() != null) {
                paymentResponse.setPaymentMethodId(payment.getPaymentMethod().getId());
                paymentResponse.setPaymentMethodName(payment.getPaymentMethod().getName());
            }

            paymentResponse.setReturnUrl(payment.getReturnUrl());
            paymentResponse.setTransactionId(payment.getTransactionId());

            response.setPayment(paymentResponse);
        }

        List<CouponUsage> couponUsages = couponUsageRepository.findByOrderId(order.getId());

        if (!couponUsages.isEmpty()) {
            response.setCouponUsages(couponUsages.stream()
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
                    .toList());
        } else {
            response.setCouponUsages(new ArrayList<>());
        }

        // Map coupon usages
//        List<OrderResponseClient.CouponResponse> couponResponses = new ArrayList<>();
//        if (order.getUser() != null) {
//            List<CouponUsage> couponUsages = couponUsageRepository.findByUserId(order.getUser().getId());
//            couponResponses = couponUsages.stream()
//                    .map(couponUsage -> new OrderResponseClient.CouponResponse(
//                            couponUsage.getId(),
//                            couponUsage.getCoupon().getCodeCoupon(),
//                            couponUsage.getCoupon().getDiscountAmount(),
//                            couponUsage.getUsedDate(),
//                            couponUsage.getCreatedBy(),
//                            couponUsage.getCreatedDate(),
//                            couponUsage.getLastModifiedBy(),
//                            couponUsage.getLastModifiedDate()
//                    ))
//                    .collect(Collectors.toList());
//        }
//        response.setCouponUsages(couponResponses);

        // Map address
        OrderResponseClient.AddressResponse addressResponse = null;
        if (!order.getIsPos() && order.getUser() != null) {
            Optional<UserAddressMapping> addressMappingOptional = userAddressMappingRepository
                    .findByUserId(order.getUser().getId())
                    .stream()
                    .findFirst();
            if (addressMappingOptional.isPresent()) {
                UserAddressMapping mapping = addressMappingOptional.get();
                Address address = mapping.getAddress();
                User user = mapping.getUser();
                addressResponse = new OrderResponseClient.AddressResponse(
                        user.getId(),
                        address.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getPhoneNumber(),
                        user.getRole().toString(),
                        address.getId(),
                        address.getStreet(),
                        address.getWard(),
                        address.getCity(),
                        address.getState(),
                        address.getCountry(),
                        address.getZipcode(),
                        address.getDistrict(),
                        address.getProvince(),
                        mapping.getReceiverName(),
                        mapping.getReceiverPhone(),
                        mapping.getIsDefault(),
                         // ✅ địa chỉ mặc định không
                        Boolean.TRUE.equals(user.getIsActive()) // tránh NPE
                );
            }
        }

        response.setAddress(addressResponse);
//        OrderResponseClient.AddressResponse addressResponse = null;
//        if (!order.getIsPos() && order.getUser() != null) {
//            Optional<UserAddressMapping> addressMappingOptional = userAddressMappingRepository
//                    .findByUserId(order.getUser().getId())
//                    .stream()
//                    .findFirst();
//            if (addressMappingOptional.isPresent()) {
//                Address address = addressMappingOptional.get().getAddress();
//                User user = addressMappingOptional.get().getUser();
//                addressResponse = new OrderResponseClient.AddressResponse(
//                        address.getId(),
//                        user.getEmail(),
//                        user.getId(),
//                        user.getUsername(),
//                        user.getPhoneNumber(),
//                        user.getRole().toString(),
//                        address.getStreet(),
//                        address.getWard(),
//                        address.getCity(),
//                        address.getState(),
//                        address.getCountry(),
//                        address.getZipcode(),
//                        address.getDistrict(),
//                        address.getProvince(),
//                        user.getIsActive()
//                );
//            }
//        }
//        response.setAddress(addressResponse);

        if (order.getIsPos() != null
                && order.getUser() != null
                && order.getUser().getId() != null) {
            if (order.getUser() != null) {
                Optional<UserAddressMapping> addressMappingOptional;

                // Nếu có addressId trong Order, lấy địa chỉ được chọn
                if (order.getAddressId() != null) {
                    addressMappingOptional = userAddressMappingRepository
                            .findByUserIdAndAddressId(order.getUser().getId(), order.getAddressId());
                } else {
                    // Nếu không có addressId, lấy địa chỉ mặc định
                    addressMappingOptional = userAddressMappingRepository
                            .findByUserId(order.getUser().getId()).stream()
                            .filter(mapping -> Boolean.TRUE.equals(mapping.getIsDefault()))
                            .findFirst();
                }

                if (addressMappingOptional.isPresent()) {
                    UserAddressMapping mapping = addressMappingOptional.get();
                    Address address = mapping.getAddress();
                    User user = mapping.getUser();

                    response.setAddress(new OrderResponseClient.AddressResponse(
                            user.getId(),
                            address.getId(),
                            user.getEmail(),
                            user.getUsername(),
                            user.getPhoneNumber(),
                            user.getRole().toString(),
                            address.getId(),
                            address.getStreet(),
                            address.getWard(),
                            address.getCity(),
                            address.getState(),
                            address.getCountry(),
                            address.getZipcode(),
                            address.getDistrict(),
                            address.getProvince(),
                            mapping.getReceiverName(),
                            mapping.getReceiverPhone(),
                            mapping.getIsDefault(),
                            user.getIsActive()
                    ));
                } else {
                    log.warn("Không tìm thấy địa chỉ cho userId: {} và addressId: {}",
                            order.getUser().getId(), order.getAddressId());
                }
            }
        }
        // Map shipment
        response.setShipments(shipmentRepository.findByOrderId(order.getId()).stream()
                .map(shipment -> {
                    OrderResponseClient.ShipmentResponse shipmentResponse = new OrderResponseClient.ShipmentResponse();
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

        return response;
    }

    private String generateOrderCode() {
        return "NAM" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String generateTrackingNumber() {
        return "TRK" + System.currentTimeMillis();
    }


}
