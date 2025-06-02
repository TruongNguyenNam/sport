package com.example.storesports.core.admin.coupon_usage.controller;


import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageRequest;
import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;
import com.example.storesports.entity.CouponUsage;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.coupon_usage.CouponUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupon_usage")
@RequiredArgsConstructor
public class CouponUsageController {

    private final CouponUsageService couponUsageService;

    @GetMapping("/{customerId}")
    public ResponseData<List<CouponUsageResponse>> getCouponsForCustomer(@PathVariable(name = "customerId") Long customerId) {
        List<CouponUsageResponse> couponUsages = couponUsageService.getCouponsForCustomer(customerId);
        return ResponseData.<List<CouponUsageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách phiếu giảm giá của khách hàng thành công")
                .data(couponUsages)
                .build();
    }

    @PostMapping("/add")
    public ResponseData<CouponUsageResponse> addCouponToUser(
            @RequestBody CouponUsageRequest request
    ) {
        Long userId = (request.getUserIds() != null && !request.getUserIds().isEmpty())
                ? request.getUserIds().get(0)
                : null;
        CouponUsageResponse response = couponUsageService.addCouponToCustomer(userId, request.getCouponId());
        return ResponseData.<CouponUsageResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thêm coupon cho user thành công")
                .data(response)
                .build();
    }

    @PostMapping("/add-multi")
    public ResponseData<List<CouponUsageResponse>> addCouponToMultipleUsers(
            @RequestBody CouponUsageRequest request
    ) {
        List<CouponUsageResponse> responses = couponUsageService.addCouponToMultipleCustomers(request.getUserIds(), request.getCouponId());
        return ResponseData.<List<CouponUsageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Thêm coupon cho nhiều user thành công")
                .data(responses)
                .build();
    }
}
