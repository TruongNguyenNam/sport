package com.example.storesports.infrastructure.constant;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public enum ShipmentStatus {
    PENDING,
    SHIPPED,
    DELIVERED,
    RETURNED,
    CANCELED
}
