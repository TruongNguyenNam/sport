package com.example.storesports.infrastructure.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED,
    SHIPPED,
    RETURNED
//    CHƯA GIẢI QUYẾT,
//    HOÀN THÀNH,
//    Bị hủy bỏ,
//    Vận chuyển,
//    Trả lại
}
