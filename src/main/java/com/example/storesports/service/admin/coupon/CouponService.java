package com.example.storesports.service.admin.coupon;


import com.example.storesports.core.admin.coupon.payload.CouponRequest;
import com.example.storesports.core.admin.coupon.payload.CouponResponse;

import java.util.List;

public interface CouponService {

    List<CouponResponse> getAll();

    List<CouponResponse> getAllActiveCoupons();

    List<CouponResponse> findByCodeCoupon(String codeCoupon);

    CouponResponse saveCoupon(CouponRequest couponRequest);

    CouponResponse updateCoupon(CouponRequest couponRequest, Long id);

    CouponResponse findById(Long id);

}
