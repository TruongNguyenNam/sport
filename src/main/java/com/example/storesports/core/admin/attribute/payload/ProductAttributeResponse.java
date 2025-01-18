package com.example.storesports.core.admin.attribute.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductAttributeResponse {
    private Long id;
    private String name;
    private String description;
}
