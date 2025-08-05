package com.example.storesports.core.auth.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO trả về thông tin phiếu giảm giá của người dùng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUserResponse {
    private Long id;                         // ID của CouponUsage
    private String couponCode;               // Mã giảm giá (từ Coupon)
    private String couponName;               // Tên giảm giá (từ Coupon)
    private Double couponDiscountAmount;     // Số tiền giảm
    private String couponStatus;             // Trạng thái ACTIVE, USED, EXPIRED (từ Coupon)
    private LocalDateTime startDate;         // Ngày bắt đầu có hiệu lực
    private LocalDateTime expiredDate;       // Ngày hết hạn
    private Date usedDate;          // Ngày sử dụng (nếu có)
}
