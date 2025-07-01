package com.example.storesports.core.client.coupon_usage.controller;

import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;
import com.example.storesports.core.client.coupon_usage.payload.CouponUsageClientResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.coupon_usage.CouponUsageClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/client/coupon_usage")
@Validated
@RequiredArgsConstructor
@Tag(name = "CouponUsage", description = "Endpoints for managing CouponUsage")
@Slf4j
public class CouponUsageClientController {
    private final CouponUsageClientService couponUsageClientService;

    @GetMapping("/{userId}")
    public ResponseData<List<CouponUsageClientResponse>> getAllCouponUsageByUserId(@PathVariable(name = "userId") Long userId) {
        List<CouponUsageClientResponse> couponUsages = couponUsageClientService.getAllCouponUsageByUserId(userId);
        return ResponseData.<List<CouponUsageClientResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách phiếu giảm giá của khách hàng thành công")
                .data(couponUsages)
                .build();
    }


}
