package com.example.storesports.core.admin.customer.controller;

import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.core.admin.customer.payload.CustomerResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseData<List<CustomerResponse>> getAllUsers() {
        List<CustomerResponse> customers = customerService.getAll();
        return ResponseData.<List<CustomerResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách khách hàng thành công")
                .data(customers)
                .build();
    }
    @GetMapping("/not-received-coupon/{couponId}")
    public ResponseData<List<CustomerResponse>> getCustomersNotReceivedCoupon(@PathVariable Long couponId) {
        List<CustomerResponse> customers = customerService.getCustomersNotReceivedCoupon(couponId);
        return ResponseData.<List<CustomerResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách khách hàng chưa nhận coupon thành công")
                .data(customers)
                .build();
    }
}
