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
    private String material;
    private String size;
    private String color;
    private String supplierName;
    private String categoryName;
    public ProductSearchRequest(String name, String sizeParam, String material, String sportType, String color, Double minPrice, Double maxPrice) {
    }

    public ProductSearchRequest(String name, String sizeParam, String material, String sportType, String color, Double minPrice, Double maxPrice,String supplierName, String categoryName) {
    }
}
