package com.example.storesports.core.client.product.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSearchClientRequest {
    private String sportType;
    private String supplierName;
    private String categoryName;
    private Double minPrice;
    private Double maxPrice;

}
