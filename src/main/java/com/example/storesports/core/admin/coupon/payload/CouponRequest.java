package com.example.storesports.core.admin.coupon.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class CouponRequest {
    private String code;
    private Double discountAmount;
    private LocalDateTime expirationDate;

}
