package com.example.storesports.infrastructure.validation;





import com.example.storesports.service.auth.JWTTokenService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;


public class RefreshTokenValidValidator implements ConstraintValidator<RefreshTokenValid,String> {
    @Autowired
    private JWTTokenService ijwtTokenService;

    @Override
    public boolean isValid(String refreshToken, ConstraintValidatorContext constraintValidatorContext) {
        if (!StringUtils.hasText(refreshToken)) {
            return false;
        }
        return ijwtTokenService.isRefreshTokenValid(refreshToken);
    }
}

