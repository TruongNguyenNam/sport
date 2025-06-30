package com.example.storesports.service.client.coupon_usage;

import com.example.storesports.core.client.coupon_usage.payload.CouponUsageClientResponse;

import java.util.List;

public interface CouponUsageClientService {
    List<CouponUsageClientResponse> getAllCouponUsageByUserId(Long userId);


}
