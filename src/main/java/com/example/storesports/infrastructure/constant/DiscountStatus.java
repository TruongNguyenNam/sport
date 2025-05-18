package com.example.storesports.infrastructure.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum DiscountStatus {
    PENDING,    // Chờ duyệt hoặc kích hoạt
    ACTIVE,     // Đang áp dụng
    INACTIVE,   // Tạm ngưng
    EXPIRED,    // Hết hạn
    DELETED     // Đã xóa
}
