package com.example.storesports.core.admin.coupon.payload;

import com.example.storesports.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CouponRequest {
    private String code;
    private Double discountAmount;
    private LocalDateTime expirationDate;
}
