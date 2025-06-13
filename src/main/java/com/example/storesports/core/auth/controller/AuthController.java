package com.example.storesports.core.auth.controller;

import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.infrastructure.validation.RefreshTokenValid;
import com.example.storesports.core.auth.payload.*;
import com.example.storesports.service.auth.impl.IAuthService;
import com.example.storesports.service.auth.impl.IJWTTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {


    private final IAuthService authService;


    private final IJWTTokenService ijwtTokenService;


    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseData<LoginInfoDto> login(@RequestBody @Valid loginForm loginForm) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginForm.getUsername(),
                        loginForm.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        LoginInfoDto loginInfoDto = authService.login(loginForm.getUsername());

        return new ResponseData<>(200, "Đăng nhập thành công", loginInfoDto);
    }


    @GetMapping("/refreshToken")
    public ResponseData<TokenDTO> refreshToken(@RefreshTokenValid String refreshToken) {
        try {
            TokenDTO newToken = ijwtTokenService.getNewToken(refreshToken);
            return new ResponseData<>(200, "Token refreshed", newToken);
        } catch (IllegalArgumentException e) {
            return new ResponseData<>(403, "Invalid refresh token");
        }
    }

    @PostMapping("/register")
    public ResponseData<UserResponse> register(@RequestBody @Valid RegisterForm registerForm) {
        UserResponse userResponse = authService.register(registerForm);
        return new ResponseData<>(201, "Đăng Ký Thành công", userResponse);
    }





}
