package com.example.storesports.core.admin.attribute.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSpecificationRequest {
    private Long id;
    private String name;
    private String description;
}
