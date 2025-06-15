package com.example.storesports.core.admin.orderItem.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SoldQuantityByMonthResponse {
    private Long quantity;
    private String month;
}