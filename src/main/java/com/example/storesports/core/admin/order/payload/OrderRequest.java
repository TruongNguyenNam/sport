package com.example.storesports.core.admin.order.payload;

import com.example.storesports.core.admin.orderItem.payload.OrderItemRequest;
import com.example.storesports.core.admin.payment.payload.PaymentRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class OrderRequest {
   // @NotBlank(message = "Order code is required")
    private String orderCode;

    //@Positive(message = "User ID must be positive if provided")
    private Long userId;

    //@NotEmpty(message = "Items are required")
    private List<OrderItemRequest> items;

    //@NotNull(message = "Payment is required")
    private PaymentRequest payment;

   // @Positive(message = "Coupon ID must be positive if provided")
    private Long couponId; // Optional, single coupon

    //@NotNull(message = "Order type (isPos) is required")
    private Boolean isPos; // True for POS, false for shipping

   // @Positive(message = "Address ID must be positive if provided for shipping")
    private Long addressId; // Required for shipping

   // @NotEmpty(message = "Shipments are required for shipping orders if isPos is false")
    private List<ShipmentRequest> shipments; // List of shipments for N-N relationship

    @Data
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long productId;

        private Integer quantity;

        private Double unitPrice;
    }

    @Data
    @NoArgsConstructor
    public static class PaymentRequest {
        private Long paymentMethodId;

        private Double amount;
    }

    @Data
    @NoArgsConstructor
    public static class ShipmentRequest {
        private String carrier;

        private String estimatedDeliveryDate;

        private List<Long> orderItemIds;
    }





}
