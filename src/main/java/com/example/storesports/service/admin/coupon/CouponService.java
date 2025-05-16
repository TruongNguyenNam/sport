package com.example.storesports.service.admin.coupon;


import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.core.admin.order.payload.OrderResponse;

import java.util.List;

public interface CouponService {

    List<CouponResponse> getAll();

}
