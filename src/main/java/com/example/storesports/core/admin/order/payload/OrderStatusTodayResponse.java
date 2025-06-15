package com.example.storesports.core.admin.order.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusTodayResponse {
    private LocalDate date;
    private long totalOrders;
}
