package com.example.storesports.infrastructure.configuration.vnpay;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
@Configuration
@Slf4j
public class VnPayConfig {
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:5173/callback"; // Thay đổi trong production
    public static String vnp_TmnCode = "5K8C7KQ5";
    public static String secretKey = "HVENZL276XFFAO6AEB2MQ1QRQFWQVSJO";
    public static String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            log.error("Lỗi khi tạo MD5 hash: {}", ex.getMessage(), ex);
            return "";
        }
    }

    public static String Sha256(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            log.error("Lỗi khi tạo SHA-256 hash: {}", ex.getMessage(), ex);
            return "";
        }
    }

    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = fields.get(key) != null ? fields.get(key) : "";
            if (key.equals("vnp_SecureHash") || key.equals("vnp_SecureHashType")) continue;
            try {
                sb.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
            } catch (UnsupportedEncodingException e) {
                log.error("Lỗi khi mã hóa giá trị: {}", e.getMessage(), e);
            }
            if (i < fieldNames.size() - 1) sb.append("&");
        }
        String rawData = sb.toString();
        log.info("Dữ liệu thô để tạo hash: {}", rawData);
        return hmacSHA512(secretKey, rawData);
    }

    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key hoặc dữ liệu rỗng");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKeySpec = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Lỗi khi tạo HMAC SHA512: {}", ex.getMessage(), ex);
            throw new RuntimeException("Không thể tạo HMAC SHA512", ex);
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "IP không hợp lệ: " + e.getMessage();
            log.error("Lỗi khi lấy địa chỉ IP: {}", e.getMessage(), e);
        }
        return ipAddress;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
