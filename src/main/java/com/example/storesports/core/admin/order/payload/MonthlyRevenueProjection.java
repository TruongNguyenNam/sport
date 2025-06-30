package com.example.storesports.core.admin.order.payload;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

public interface MonthlyRevenueProjection {
    String getMonth();
    BigDecimal getTotalRevenue();
}
