package com.example.storesports.service.auth;


import com.example.storesports.core.auth.payload.LoginInfoDto;
import com.example.storesports.core.auth.payload.RegisterForm;
import com.example.storesports.core.auth.payload.UpdateUserForm;
import com.example.storesports.core.auth.payload.UserResponse;

public interface AuthService {
    LoginInfoDto login(String username);

    UserResponse register(RegisterForm registerForm);

    UserResponse updateAddress(Long userId, UpdateUserForm userForm);

    UserResponse finById(Long id);

}
