package com.example.storesports.core.admin.order.payload;

import com.example.storesports.core.admin.orderItem.payload.OrderItemRequest;
import com.example.storesports.core.admin.payment.payload.PaymentRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderRequest {

    private String orderCode;

    private Long userId;

    private String nodes;

    private List<OrderItemRequest> items;

    private PaymentRequest payment;

    private List<Long> couponUsageIds;

    private List<ShipmentRequest> shipments;



    @Data
    @NoArgsConstructor
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
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
        private Long carrierId;
        private Double shippingCost;
        private LocalDateTime estimatedDeliveryDate;
        private List<Long> orderItemIds;
    }

}
