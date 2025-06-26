package com.example.storesports.core.client.shopping_cart.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderRequestClient {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId; // ID người dùng, khớp với ShoppingCartRequest

    private String nodes; // Ghi chú đơn hàng (tùy chọn)

    private List<OrderItemRequest> items; // Danh sách sản phẩm (tùy chọn, lấy từ giỏ hàng nếu rỗng)

//    @NotNull(message = "Payment is required")
    private PaymentRequest payment; // Thông tin thanh toán

    private List<Long> couponUsageIds; // Danh sách ID của coupon_usage (tùy chọn)

//    @NotNull(message = "Shipments are required for online orders")
    private List<ShipmentRequest> shipments; // Thông tin vận chuyển (bắt buộc)

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
//        @NotNull(message = "Product ID is required")
//        @Positive(message = "Product ID must be positive")
        private Long productId;
//        @NotNull(message = "Quantity is required")
//        @Positive(message = "Quantity must be positive")
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequest {
//        @NotNull(message = "Payment method ID is required")
//        @Positive(message = "Payment method ID must be positive")
        private Long paymentMethodId;
//        @NotNull(message = "Amount is required")
//        @Positive(message = "Amount must be positive")
        private Double amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentRequest {
//        @NotNull(message = "Carrier ID is required")
//        @Positive(message = "Carrier ID must be positive")
        private Long carrierId;
//        @NotNull(message = "Estimated delivery date is required")
        private LocalDateTime estimatedDeliveryDate;
        private List<Long> orderItemIds; // Sẽ được gán sau khi tạo order_item
    }


}
