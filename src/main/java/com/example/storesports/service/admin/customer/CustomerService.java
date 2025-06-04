package com.example.storesports.service.admin.customer;

import com.example.storesports.core.admin.customer.payload.CustomerResponse;
import com.example.storesports.infrastructure.constant.Role;

import java.util.List;

public interface CustomerService {

    List<CustomerResponse> getAll();


    List<CustomerResponse> getCustomersNotReceivedCoupon(Long couponId);
}
