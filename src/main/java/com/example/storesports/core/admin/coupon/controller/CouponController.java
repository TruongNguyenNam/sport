package com.example.storesports.core.admin.coupon.controller;

import com.example.storesports.core.admin.coupon.payload.CouponRequest;
import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupon")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping
    public ResponseData<List<CouponResponse>> getAllCoupon() {
        List<CouponResponse> couponResponses = couponService.getAllActiveCoupons();
        return ResponseData.<List<CouponResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách phiếu giảm giá thành công")
                .data(couponResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseData<CouponResponse> getCouponById(@PathVariable Long id) {
        CouponResponse coupon = couponService.findById(id);
        return ResponseData.<CouponResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin coupon thành công")
                .data(coupon)
                .build();
    }

    @PostMapping("/add")
    public ResponseData<CouponResponse> addCoupon(@RequestBody CouponRequest couponRequest) {
        CouponResponse coupon = couponService.saveCoupon(couponRequest);
        return ResponseData.<CouponResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Thêm mới coupon thành công")
                .data(coupon)
                .build();
    }

    @GetMapping("/search")
    public ResponseData<List<CouponResponse>> searchCoupons(@RequestParam String codeCoupon) {
        List<CouponResponse> coupons = couponService.findByCodeCoupon(codeCoupon);
        return ResponseData.<List<CouponResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Tìm kiếm coupon thành công")
                .data(coupons)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseData<CouponResponse> updateCoupon(@RequestBody CouponRequest couponRequest, @PathVariable Long id) {
        CouponResponse coupon = couponService.updateCoupon(couponRequest, id);
        return ResponseData.<CouponResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật coupon thành công")
                .data(coupon)
                .build();
    }


}
