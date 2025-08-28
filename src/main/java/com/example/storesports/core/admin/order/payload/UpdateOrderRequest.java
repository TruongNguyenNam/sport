package com.example.storesports.core.admin.order.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class UpdateOrderRequest {
    private String orderCode;
    private Long userId;
    private String nodes;
    private List<OrderItemUpdate> items;
    private PaymentRequest payment;
    private List<Long> couponUsageIds;
    private List<ShipmentRequest> shipments;

    @Data
    @NoArgsConstructor
    public static class OrderItemUpdate {
        private Long productId;
        private Integer quantity; // 0 để xóa, >0 để thêm/cập nhật
    }

    @Data
    @NoArgsConstructor
    public static class PaymentRequest {
        private Long paymentMethodId;
        private Double amount;
        private Double changeAmount;
    }

    @Data
    @NoArgsConstructor
    public static class ShipmentRequest {
        private Long carrierId;
        private Double shippingCost;
        private LocalDateTime estimatedDeliveryDate;
    }

}
