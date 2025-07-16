package com.example.storesports.infrastructure.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum ShipmentStatus {
    PENDING, // xác nhận
    SHIPPED, // đang giao
    DELIVERED,  // đã giao hàng
    RETURNED, // trả hàng
    CANCELED // huỷ
}
