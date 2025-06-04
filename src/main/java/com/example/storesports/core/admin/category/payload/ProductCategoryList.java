package com.example.storesports.core.admin.category.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductCategoryList {

    private Long id;
    private String name;
    private String description;

}
