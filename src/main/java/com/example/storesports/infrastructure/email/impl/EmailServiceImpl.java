package com.example.storesports.infrastructure.email.impl;

import com.example.storesports.infrastructure.email.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendCouponAwardedEmail(String to, String couponName, String couponCode, String expiryDate, String discountAmount) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("yourname@gmail.com", "Shop ShoesSpost");
            helper.setTo(to);
            helper.setSubject("Bạn đã nhận được coupon mới!");

            String body = String.format(
                    "<p><strong>Shop ShoesSpost</strong></p>" +
                            "<p>Chúc mừng! Bạn vừa nhận được coupon mới:</p>" +
                            "<ul>" +
                            "<li><strong>Tên coupon:</strong> %s</li>" +
                            "<li><strong>Mã coupon:</strong> %s</li>" +
                            "<li><strong>Trị giá giảm:</strong> %s</li>" +
                            "<li><strong>Hạn sử dụng:</strong> %s</li>" +
                            "</ul>" +
                            "<p>Chúc bạn mua sắm vui vẻ!</p>",
                    couponName, couponCode, discountAmount, expiryDate
            );
            helper.setText(body, true); // gửi HTML

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi email thất bại:");
            e.printStackTrace();
        }
    }


    // gui tk mk
    @Override
    public void sendAccountInfo(String to, String username, String rawPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("yourname@gmail.com", "Shop ShoesSpost");
            helper.setTo(to);
            helper.setSubject("Tài khoản khách hàng của bạn");

            String body = String.format(
                    "<p>Shop ShoesSpost</p>" +
                            "<p>Chào <strong>%s</strong>,</p>" +
                            "<p>Tài khoản của bạn đã được tạo thành công.</p>" +
                            "<p><strong>Tên đăng nhập:</strong> %s<br/>" +
                            "<strong>Mật khẩu:</strong> %s</p>" +
                            "<p>Hãy đăng nhập và thay đổi mật khẩu sớm nhất!</p>" +
                            "<p>Trân trọng,<br/>Đội ngũ hỗ trợ.</p>",
                    username, username, rawPassword
            );
            helper.setText(body, true); // true = gửi HTML

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi email thất bại:");
            e.printStackTrace();
        }
    }

}