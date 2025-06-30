package com.example.storesports.core.admin.customer.controller;

import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.core.admin.customer.payload.CustomerRequest;
import com.example.storesports.core.admin.customer.payload.CustomerResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    //thêm
    @PostMapping("/add")
    public ResponseData<CustomerResponse> createCustomer(@RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.createCustomer(request);
        return ResponseData.<CustomerResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Tạo mới khách hàng thành công")
                .data(created)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseData<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRequest request
    ) {
        CustomerResponse updated = customerService.updateCustomer(id, request);
        return ResponseData.<CustomerResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật khách hàng thành công")
                .data(updated)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseData<CustomerResponse> getCouponById(@PathVariable Long id) {
        CustomerResponse customerById = customerService.getCustomerById(id);
        return ResponseData.<CustomerResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin coupon thành công")
                .data(customerById)
                .build();
    }
}
