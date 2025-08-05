package com.example.storesports.service.auth;


import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;
import com.example.storesports.core.auth.payload.CouponUserResponse;
import com.example.storesports.core.auth.payload.LoginInfoDto;
import com.example.storesports.core.auth.payload.RegisterForm;
import com.example.storesports.core.auth.payload.UpdateUserForm;
import com.example.storesports.core.auth.payload.UserResponse;

import java.util.List;

public interface AuthService {
    LoginInfoDto login(String username);

    UserResponse register(RegisterForm registerForm);

    UserResponse updateAddress(Long userId, UpdateUserForm userForm);

    UserResponse finById(Long id);

    UserResponse updateUserInfo(Long userId, UpdateUserForm userForm);

    List<CouponUserResponse> getCouponsForUser(Long customerId);

}
