package com.example.storesports.core.admin.order.payload;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonPropertyOrder({"totalRevenue","totalSoldQuantity", "completedOrders", "cancelledOrders", "returnedOrders"})
public class CustomStatisticalResponse {
    private Double totalRevenue;
    private Long totalSoldQuantity;
    private Long completedOrders;
    private Long cancelledOrders;
    private Long returnedOrders;
}
