package com.example.storesports.service.admin.coupon_usage;

import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;

import java.util.List;

public interface CouponUsageService {

    List<CouponUsageResponse> getCouponsForCustomer(Long customerId);


    CouponUsageResponse addCouponToCustomer(Long userId, Long couponId);

    List<CouponUsageResponse> addCouponToMultipleCustomers(List<Long> userIds, Long couponId);

}
