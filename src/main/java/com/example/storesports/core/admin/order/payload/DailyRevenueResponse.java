package com.example.storesports.core.admin.order.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueResponse {
    private LocalDate day;
    private BigDecimal totalRevenue;
}
