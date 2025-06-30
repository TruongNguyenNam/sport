package com.example.storesports.core.admin.order.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusYearResponse {
    private int year;
    private long totalOrders;
}
