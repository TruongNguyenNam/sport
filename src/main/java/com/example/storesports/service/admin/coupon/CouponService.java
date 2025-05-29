package com.example.storesports.service.admin.coupon;


import com.example.storesports.core.admin.coupon.payload.CouponRequest;
import com.example.storesports.core.admin.coupon.payload.CouponResponse;

import java.util.List;

public interface CouponService {

    List<CouponResponse> findAllCoupon();

    CouponResponse saveCoupon(CouponRequest couponRequest);

    CouponResponse updateCoupon(CouponRequest couponRequest, Long id);

    CouponResponse softDeleteCoupon(Long id);

    List<CouponResponse> findByCriteria(String code, Double discountAmount);

    CouponResponse findById(Long id);

}
