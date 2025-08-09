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

    @Override
    public void sendReturnRequestStatusEmail(
            String to,
            String customerName,
            String orderCode,
            String returnCode,
            String status,
            String rejectedReason, // dùng nếu bị từ chối
            String returnAddress,
            String returnPhone,
//            String carrierName,
            String shopName
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);

        if ("APPROVED".equalsIgnoreCase(status)) {
            message.setSubject("Yêu cầu hoàn hàng đã được duyệt");

            String body = String.format(
                    "Chào %s,\n\n" +
                            "Yêu cầu hoàn hàng của bạn cho đơn hàng #%s đã được duyệt thành công.\n\n" +
                            "- Mã hoàn trả: %s\n" +
                            "- Người nhận: %s\n" +
                            "- Địa chỉ nhận: %s\n" +
                            "- SĐT nhận: %s\n" +
//                            "- Đơn vị vận chuyển: %s\n\n" +
                            "Vui lòng ghi rõ mã hoàn trả: %s trên gói hàng khi gửi.\n\n" +
                            "Cảm ơn bạn đã sử dụng dịch vụ tại %s!\n\n" +
                            "Trân trọng,\n%s Team",
                    customerName, orderCode, returnCode,
                    "Nguyễn Nam Trường", returnAddress, returnPhone,
//                    carrierName,
                    returnCode, shopName, shopName
            );
            message.setText(body);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            message.setSubject("Yêu cầu hoàn hàng bị từ chối");

            String body = String.format(
                    "Chào %s,\n\n" +
                            "Rất tiếc, yêu cầu hoàn hàng cho đơn hàng #%s đã bị từ chối.\n\n" +
                            "- Lý do từ chối: %s\n\n" +
                            "Nếu bạn cần hỗ trợ thêm, vui lòng liên hệ chúng tôi.\n\n" +
                            "Trân trọng,\n%s Team",
                    customerName, orderCode, rejectedReason, shopName
            );
            message.setText(body);
        }
    else {
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
    }


        mailSender.send(message);
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