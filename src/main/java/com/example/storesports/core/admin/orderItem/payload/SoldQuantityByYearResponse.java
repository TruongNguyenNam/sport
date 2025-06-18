package com.example.storesports.core.admin.orderItem.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SoldQuantityByYearResponse {
    private Long quantity;
    private int year; // ví dụ: 2025
}