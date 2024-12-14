package com.example.storesports.core.admin.product.payload;

import com.example.storesports.entity.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String sportType;
    private String material;
    private String size;
    private String color;
    private String sku;
    private String supplierName;
    private String categoryName;
    private List<String> sportTypeName;
    private List<ProductSpecificationOptionResponse> productSpecificationOptionResponses;
    private List<String> tagName;
    private List<String> ImageUrl;
    private List<InventoryResponse> inventories;

    @Data
    @NoArgsConstructor
    public static class ProductSpecificationOptionResponse {
        private Long id;
        private String specificationName;
        private Long productId;
        private String value;
    }


    @Data
    @NoArgsConstructor
    public static class InventoryResponse {
        private Long id;
        private String productName;
        private String stockQuantity;
    }


}
