package com.example.storesports.core.client.shopping_cart.payload;

import com.example.storesports.core.client.product.payload.ProductResponseClient;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartResponse {

    private Long id;

    private String userName;

    private Product product;

    private Integer quantity;

    private Double totalPrice;

    private Boolean deleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Double originalPrice;
        private Integer stockQuantity;
        private Long parentProductId;
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

}
