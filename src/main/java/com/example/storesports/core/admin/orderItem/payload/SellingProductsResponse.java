package com.example.storesports.core.admin.orderItem.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellingProductsResponse {
    private Long id;
    private String imgUrl;
    private String productName;
    private Long soldQuantity;
    private Double percentage;
    private LocalDate orderDate;
}
