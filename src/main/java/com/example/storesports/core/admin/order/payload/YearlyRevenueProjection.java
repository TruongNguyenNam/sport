package com.example.storesports.core.admin.order.payload;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

public interface YearlyRevenueProjection {
    String getYear();
    BigDecimal getTotalRevenue();
}
