package com.example.storesports.core.admin.product.payload;

import com.example.storesports.entity.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String sportType;
    private String material;
    private String size;
    private String color;
    private String sku; //UUID
    private Long supplierId;
    private Long categoryId;
    private List<Long> sportTypeId;
    private List<Long> tagId;
    private List<ProductSpecificationOption> productSpecificationOptions = new ArrayList<>();
    private List<Long> productImageIds;
    private List<Long> inventoryIds;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductSpecificationOption {
        private Long specificationId;
        private Long productId;
        private String value;
    }

}
