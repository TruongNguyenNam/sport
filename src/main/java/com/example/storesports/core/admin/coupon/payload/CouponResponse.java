package com.example.storesports.core.admin.coupon.payload;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class CouponResponse {
    private Long id;
    private String code;
    private Integer quantity;
    private String couponStatus;
    private Double discountAmount;
    private LocalDateTime expirationDate;
    private LocalDateTime statDate;
    private Boolean status;

}
