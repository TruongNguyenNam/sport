package com.example.storesports.core.admin.order.payload;

public interface MonthlyOrderTypeProjection {
    Integer getMonth();      // tháng
    Boolean getIsPos();      // true: bán thường, false: bán ship
    Long getTotalOrders();   // tổng số đơn hàng
}
