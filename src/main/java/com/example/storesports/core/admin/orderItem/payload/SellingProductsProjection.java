package com.example.storesports.core.admin.orderItem.payload;

import java.time.LocalDate;


public interface SellingProductsProjection {
    Long getId();
    String getImgUrl();
    String getProductName();
    Long getSoldQuantity();
    Double getPercentage();
    LocalDate getOrderDate();
}
