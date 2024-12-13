package com.example.storesports.Infrastructure.constant;

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
