package com.example.storesports.service.admin.customer;

import com.example.storesports.core.admin.customer.payload.CustomerRequest;
import com.example.storesports.core.admin.customer.payload.CustomerResponse;
import com.example.storesports.infrastructure.constant.Role;

import java.util.List;

public interface CustomerService {


    CustomerResponse getCustomerById(Long id);

    List<CustomerResponse> getAll();

    List<CustomerResponse> getCustomersNotReceivedCoupon(Long couponId);

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long customerId, CustomerRequest request);
}
