package com.example.storesports.infrastructure.email;


public interface EmailService {

        void sendCouponAwardedEmail(String to, String couponName, String couponCode, String expiryDate, String discountAmount);
      void sendReturnRequestStatusEmail(
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
    );

}

