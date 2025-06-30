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
    private Long userId;

    private String nodes;

    private List<OrderItemRequest> items;

    private PaymentRequest payment;

    private List<Long> couponUsageIds;

    private List<ShipmentRequest> shipments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        private Long productId;

        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequest {
        private Long paymentMethodId;
        private Double amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipmentRequest {
        private Long carrierId;
        private LocalDateTime estimatedDeliveryDate;
        private List<Long> orderItemIds;
    }


}
