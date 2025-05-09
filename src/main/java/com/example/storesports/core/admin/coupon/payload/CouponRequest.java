package com.example.storesports.core.admin.coupon.payload;

import lombok.Data;

@Data
public class CouponRequest {
    private Long id;

    private String code;
    private Double discountAmount;
    private java.util.Date expirationDate;



}
