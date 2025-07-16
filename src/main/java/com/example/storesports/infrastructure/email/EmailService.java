package com.example.storesports.infrastructure.email;


public interface EmailService {

        void sendCouponAwardedEmail(String to, String couponName, String couponCode, String expiryDate, String discountAmount);
        
        void sendAccountInfo(String to, String username, String rawPassword);

}

