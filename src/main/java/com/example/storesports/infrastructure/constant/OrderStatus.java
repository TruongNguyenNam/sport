package com.example.storesports.infrastructure.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum OrderStatus {
    PENDING, //CHỜ XÁC NHẬN
    COMPLETED, //HOÀN THÀNH
    CANCELLED, // BỊ HUỶ BỎ
    SHIPPED, // ĐANG GIAO
    RETURNED // TRẢ HÀNG
}
