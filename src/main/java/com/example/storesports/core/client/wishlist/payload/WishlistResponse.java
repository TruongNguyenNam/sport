package com.example.storesports.core.client.wishlist.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {


    private Long id;

    private String userName;

    private List<Product> product;

    private Date addedDate;

    private Boolean deleted;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static  class Product{
        private Long id;
        private String name;
        private String description;
        private Double price;
        private Double originalPrice;
        private Integer stockQuantity;
//        parentProductId: number | null;
        private Long parentProductId;
        private String sportType;
        private String sku;
        private String supplierName;
        private String categoryName;
        private List<ProductAttributeValueResponse> productAttributeValueResponses;
        private List<String> tagName;
        private List<String> ImageUrl;
    }

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
