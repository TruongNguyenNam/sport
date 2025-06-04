package com.example.storesports.core.admin.discount.payload;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscountResponse {
    private Long id;
    private String name;
    private String discountPercentage;
    private int countProduct;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
   private List<ProductResponse> productResponses;
}
