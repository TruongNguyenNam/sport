package com.example.storesports.Infrastructure.constant;

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
}
