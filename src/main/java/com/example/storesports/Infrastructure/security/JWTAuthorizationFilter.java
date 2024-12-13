package com.example.storesports.Infrastructure.security;



import com.example.storesports.service.auth.IJWTTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
public class JWTAuthorizationFilter extends GenericFilterBean {
    @Autowired
    private IJWTTokenService ijwtTokenService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String token = request.getHeader("Authorization"); // Sử dụng tên đúng của tiêu đề

        if (token != null && token.startsWith("Bearer ")) {
            // Loại bỏ tiền tố "Bearer " khỏi token
            String jwtToken = token.substring(7);

            Authentication authentication = ijwtTokenService.parseTokenToUserInformation(request);
            if (authentication != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    }


