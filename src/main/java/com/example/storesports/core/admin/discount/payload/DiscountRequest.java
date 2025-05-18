package com.example.storesports.core.admin.discount.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscountRequest {
    private String name;
    private Double percentValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> categoryId;
    private Double priceThreshold;
    private Boolean applyToAll;
    private List<Long> productIds;

}
