package com.example.storesports.core.admin.order.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueResponse {
    private YearMonth month;
    private BigDecimal totalRevenue;

}
