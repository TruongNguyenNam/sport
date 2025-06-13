package com.example.storesports.service.auth.impl;


import com.example.storesports.core.auth.payload.TokenDTO;
import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import com.example.storesports.repositories.TokenRepository;
import com.example.storesports.service.auth.JWTTokenService;
import com.example.storesports.service.auth.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class IJWTTokenService implements JWTTokenService {


    private final UserService service;
    private final TokenRepository tokenRepository;

    @Value("${jwt.token.secret}")
    private String jwtSecret;

    @Value("${jwt.token.expiration.time}")
    private Long expirationTime;

    @Value("${jwt.token.refresh.expiration.time}")
    private Long REFRESH_EXPIRATION_TIME;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


//    @Override
//    @Transactional
//    public String generateJWT(String username) {
//        Date now = new Date();
//        Claims claims = Jwts.claims().setSubject(username);
//        log.info("Tạo token cho user: {}, thời gian: {}", username, now);
//        String token = Jwts.builder()
//                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(new Date(now.getTime() + expirationTime))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
//                .compact();
//        log.info("token : " + token);
//        return token;
//    }

    @Override
    @Transactional
    public String generateJWT(String username) {
        Date now = new Date();
        User user = service.getAccountByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng: " + username);
        }
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("userId", Math.toIntExact(user.getId()));
        log.info("Tạo token cho user: {}, userId: {}, thời gian: {}", username, user.getId(), now);
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        log.info("Token: {}", token);
        return token;
    }

    @Override
    @Transactional
    public Authentication parseTokenToUserInformation(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }

        try {
            String jwtToken = token.substring(7);

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            String username = claims.getSubject();
            User user = service.getAccountByUsername(username);

            if (user == null) {
                return null;
            }

            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    AuthorityUtils.createAuthorityList(user.getRole().toString())
            );

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(claims);
            return auth;
        } catch (Exception e) {
            log.error("Lỗi khi phân tích JWT: {}", e.getMessage(), e);
            return null;
        }
    }

//    @Override
//    @Transactional
//    public Authentication parseTokenToUserInformation(HttpServletRequest request) {
//        String token = request.getHeader("Authorization");
//
//        if (token == null || !token.startsWith("Bearer ")) {
//            return null;
//        }
//
//        try {
//            String jwtToken = token.substring(7);
//
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(getSigningKey())
//                    .setAllowedClockSkewSeconds(60) // Cho phép lệch thời gian
//                    .build()
//                    .parseClaimsJws(jwtToken)
//                    .getBody();
//
//            String username = claims.getSubject();
//            User user = service.getAccountByUsername(username);
//
//            if (user == null) {
//                return null;
//            }
//
//            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
//                    user.getUsername(),
//                    user.getPassword(),
//                    AuthorityUtils.createAuthorityList(user.getRole().toString())
//            );
//
//            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//        } catch (Exception e) {
//            log.error("Error parsing JWT: {}", e.getMessage());
//            return null;
//        }
//    }

    @Override
    @Transactional
    public Token generateRefreshToken(User user) {
        try {                                           // tạo ra RefreshToken dựa vào account
            // Tạo token mới
            Token refreshToken = new Token(
                    user,
                    UUID.randomUUID().toString(),
                    Token.Type.REFRESH_TOKEN,
                    new Date(new Date().getTime() + REFRESH_EXPIRATION_TIME)
            );

            // Xóa các token cũ của account
            tokenRepository.deleteByUser(user);

            // Lưu token mới và trả về
            return tokenRepository.save(refreshToken);
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra
            e.printStackTrace();
            throw new RuntimeException("Không thể tạo token mới.");
        }
    }

    @Override
    @Transactional
    public Boolean isRefreshTokenValid(String refreshToken) {
        Token entity = tokenRepository.findByKeyAndType(refreshToken, Token.Type.REFRESH_TOKEN);
        if(entity == null || entity.getExpiredDate().before(new Date())){
            throw new IllegalArgumentException("Token không hợp lệ.");
        }
        return true;
    }

    @Override
    @Transactional
    public TokenDTO getNewToken(String refreshToken) {
        Token oldRefreshToken = tokenRepository.findByKeyAndType(refreshToken, Token.Type.REFRESH_TOKEN);

        if (oldRefreshToken == null || oldRefreshToken.getExpiredDate().before(new Date())) {
            throw new IllegalArgumentException("Refresh Token không hợp lệ hoặc đã hết hạn.");
        }

        // Xoá các token cũ
        tokenRepository.deleteByUser(oldRefreshToken.getUser());

        // Tạo ra RefreshToken mới
        Token newRefreshToken = generateRefreshToken(oldRefreshToken.getUser());

        // Tạo ra Token mới sau thời gian tạo ra
        String newToken = generateJWT(oldRefreshToken.getUser().getUsername());

        return new TokenDTO(newToken, newRefreshToken.getKey());
    }
}
