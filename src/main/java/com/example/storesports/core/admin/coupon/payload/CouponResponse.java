package com.example.storesports.core.admin.coupon.payload;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private Long id;
    private String couponName;
    private String codeCoupon;
    private Double couponAmount;
    private String couponStatus;
    private Integer quantity;
    private LocalDateTime startDate;
    private LocalDateTime expirationDate;
    private Boolean deleted;
    private Long usedCount;
}