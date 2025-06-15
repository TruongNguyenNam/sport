package com.example.storesports.core.admin.orderItem.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SoldQuantityResponse {
    private Long quantity;
    private LocalDate date;
}
