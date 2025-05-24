package com.example.storesports.core.admin.product.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductChild {
    private Long parentProductId;
    private List<ProductAttributeValue> productAttributeValues= new ArrayList<>();
    private List<ProductVariant> variants = new ArrayList<>();
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductAttributeValue {
        private Long attributeId;
        private String value;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductVariant {
        private Double price;
        private Integer stockQuantity;
        private List<MultipartFile> images;
    }


}
