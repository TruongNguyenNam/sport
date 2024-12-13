package com.example.storesports.service.auth;


import com.example.storesports.core.auth.payload.TokenDTO;
import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;


public interface JWTTokenService {
    String generateJWT(String username);

    Authentication parseTokenToUserInformation(HttpServletRequest request);

    Token generateRefreshToken(User user);

    Boolean isRefreshTokenValid(String refreshToken);

    TokenDTO getNewToken(String refreshToken);
}
