package com.example.storesports.infrastructure.email.impl;

import com.example.storesports.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendCouponAwardedEmail(String to, String couponName, String couponCode, String expiryDate, String discountAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Bạn đã nhận được coupon mới!");

        String body = String.format(
                "Chúc mừng! Bạn vừa nhận được coupon mới:\n" +
                        "Tên coupon: %s\n" +
                        "Mã coupon: %s\n" +
                        "Trị giá giảm: %s\n" +
                        "Hạn sử dụng: %s\n\n" +
                        "Chúc bạn mua sắm vui vẻ!",
                couponName, couponCode, discountAmount, expiryDate
        );
        message.setText(body);
        mailSender.send(message);
    }
}