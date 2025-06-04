package com.example.storesports.core.admin.coupon_usage.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class CouponUsageResponse {

    private Long id;

    private String userUserName;

    private String userRole;

    private String couponCode;

    private Double couponDiscountAmount;

    private String CouponStatus;

    private Date usedDate;


}
