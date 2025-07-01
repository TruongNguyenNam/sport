package com.example.storesports.core.client.coupon_usage.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@NoArgsConstructor
public class CouponUsageClientResponse {
    private Long id;

    private String userName;

    private String userRole;

    private String couponCode;

    private Double couponDiscountAmount;

    private String CouponStatus;

    private Date usedDate;

}
