package com.example.storesports.core.admin.coupon.controller;

import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupon")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping
    public ResponseData<List<CouponResponse>> getAllCoupon(){
        List<CouponResponse> couponResponses = couponService.getAll();
        return ResponseData.<List<CouponResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách phiếu giảm giá thành công")
                .data(couponResponses)
                .build();
    }








}
