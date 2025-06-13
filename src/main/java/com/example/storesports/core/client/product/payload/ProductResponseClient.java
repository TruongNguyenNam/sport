package com.example.storesports.core.client.product.payload;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class ProductResponseClient {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double originalPrice;
    private Integer stockQuantity;
    private String sportType;
    private String sku;
    private String supplierName;
    private String categoryName;
    private List<ProductAttributeValueResponse> productAttributeValueResponses;
    private List<String> tagName;
    private List<String> ImageUrl;

    @Data
    @NoArgsConstructor
    public static class ProductAttributeValueResponse {
        private Long id;
        private Long attributeId;
        private String attributeName;
        private Long productId;
        private String value;
    }


}
