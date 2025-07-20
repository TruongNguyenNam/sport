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
                "Shop ShoesSpost\n" +
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
            String carrierName,
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
                            "- Đơn vị vận chuyển: %s\n\n" +
                            "Vui lòng ghi rõ mã hoàn trả: %s trên gói hàng khi gửi.\n\n" +
                            "Cảm ơn bạn đã sử dụng dịch vụ tại %s!\n\n" +
                            "Trân trọng,\n%s Team",
                    customerName, orderCode, returnCode,
                    shopName, returnAddress, returnPhone,
                    carrierName, returnCode, shopName, shopName
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


}