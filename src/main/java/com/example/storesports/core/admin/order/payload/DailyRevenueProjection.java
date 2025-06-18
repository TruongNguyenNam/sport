package com.example.storesports.core.admin.order.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueProjection {
    LocalDate getDay();
    BigDecimal getTotalRevenue();
}
