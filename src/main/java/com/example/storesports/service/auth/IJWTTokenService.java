package com.example.storesports.service.auth;


import com.example.storesports.core.auth.payload.TokenDTO;
import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import com.example.storesports.repositories.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class IJWTTokenService implements JWTTokenService {

    @Autowired
    private UserService service;

    @Autowired
    private TokenRepository tokenRepository;

    @Value("${jwt.token.expiration.time}")
    private Long expirationTime;


    @Value("${jwt.token.refresh.expiration.time}")
    private Long REFRESH_EXPIRATION_TIME;

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    @Override
    @Transactional
    public String generateJWT(String username) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(username);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(key) // Use the generated key
                .compact();
    }

    @Override
    @Transactional
    public Authentication parseTokenToUserInformation(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer")) {
            return null;
        }

        try {
            // Remove "Bearer " prefix
            String jwtToken = token.substring(7);

            // Parse token and extract username
            String username = Jwts.parserBuilder()
                    .setSigningKey(key) // Use the generated key
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody()
                    .getSubject();

            // Get account details
            User user = service.getAccountByUsername(username);

            return username != null ? new UsernamePasswordAuthenticationToken(user.getUsername(), null,
                    AuthorityUtils.createAuthorityList(user.getRole().toString())) : null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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
