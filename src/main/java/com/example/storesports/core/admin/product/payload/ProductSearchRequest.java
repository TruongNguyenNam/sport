package com.example.storesports.core.admin.product.payload;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String name;
    private Double minPrice;
    private Double maxPrice;
    private String sportType;
    private String supplierName;
    private String categoryName;

}
