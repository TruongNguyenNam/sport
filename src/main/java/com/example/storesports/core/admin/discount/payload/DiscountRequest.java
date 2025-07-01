package com.example.storesports.core.admin.discount.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "ko được để trống name")
    private String name;
    @NotNull(message = "ko được để trống percentValue")
    private Double percentValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;


    @NotNull(message = "ko được để trống priceThreshold")
    private Double priceThreshold;
    private Boolean applyToAll;
    private List<Long> productIds;

}
