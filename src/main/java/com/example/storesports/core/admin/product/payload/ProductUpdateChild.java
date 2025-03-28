package com.example.storesports.core.admin.product.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateChild {

    private String name;
    private String description;
    private String sportType;
    private String sku;
    private Double price;
    private Integer stockQuantity;
    private Long supplierId;
    private Long categoryId;
    private List<ProductAttributeValue> productAttributeValues;
    private List<MultipartFile> images;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductAttributeValue {
        private Long attributeId;
        private String value;
    }



}
