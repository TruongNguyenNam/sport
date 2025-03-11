package com.example.storesports.core.admin.product.payload;

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
    private String sku;
    private String supplierName;
    private String categoryName;
    private List<ProductAttributeValueResponse> productAttributeValueResponses;
    private List<String> tagName;
    private List<String> ImageUrl;
    private List<InventoryResponse> inventories;

    @Data
    @NoArgsConstructor
    public static class ProductAttributeValueResponse {
        private Long id;
        private String attributeName;
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