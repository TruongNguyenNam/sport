package com.example.storesports.core.admin.coupon.payload;

import com.example.storesports.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CouponRequest {

    private String couponName;
    private Double discountAmount;
    private Integer quantity;
    private String couponStatus; // Enum dạng String ("ACTIVE", "INACTIVE"...)
    private LocalDateTime startDate;
    private LocalDateTime expirationDate;

}

