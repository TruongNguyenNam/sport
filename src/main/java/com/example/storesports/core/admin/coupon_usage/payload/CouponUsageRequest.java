package com.example.storesports.core.admin.coupon_usage.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class CouponUsageRequest {

    private Long couponId;
    private List<Long> userIds;


}
