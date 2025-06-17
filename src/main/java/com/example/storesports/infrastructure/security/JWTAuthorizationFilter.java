package com.example.storesports.infrastructure.security;



import com.example.storesports.service.auth.impl.IJWTTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthorizationFilter extends GenericFilterBean {


    private final IJWTTokenService ijwtTokenService;

    @Value("${jwt.token.authorization}")
    private String authorizationHeader;

    @Value("${jwt.token.prefix}")
    private String tokenPrefix;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Lấy đường dẫn yêu cầu
        String requestPath = request.getRequestURI();
        log.info("Request URI: {}", requestPath);

        // Bỏ qua filter cho các endpoint công khai
        if (requestPath.startsWith("/api/v1/auth/register") || requestPath.startsWith("/api/v1/auth/login")) {
            log.debug("Bỏ qua JWT filter cho endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(authorizationHeader);
        log.info("Raw Authorization Header: {}", token);

        // Bỏ qua nếu không có token hoặc token không bắt đầu bằng "Bearer "
        if (token == null || !token.startsWith(tokenPrefix + " ")) {
            log.debug("Không tìm thấy JWT token hợp lệ trong header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwtToken = token.replace(tokenPrefix + " ", "").trim();
            log.info("Extracted JWT Token: {}", jwtToken);

            Authentication authentication = ijwtTokenService.parseTokenToUserInformation(request);
            if (authentication != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Đã đặt Authentication cho user: {}", authentication.getName());
            } else {
                log.warn("Authentication null hoặc đã được đặt");
            }
        } catch (Exception e) {
            log.error("Lỗi xử lý JWT: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    }


