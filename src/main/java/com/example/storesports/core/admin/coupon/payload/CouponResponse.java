package com.example.storesports.core.admin.coupon.payload;

import lombok.Data;

import java.util.Date;

@Data
public class CouponResponse {

    private Long id;

    private String code;
    private Double discountAmount;
    private Date expirationDate;

}
