package com.example.storesports.core.admin.product.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateChild {
    private Long id; // thêm thử
    private String name;
    private String description;
    private String sportType;
    private String sku;
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private Double price;
    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer stockQuantity;
    private Long supplierId;
    private Long categoryId;
    private List<ProductAttributeValue> productAttributeValues;
    private List<MultipartFile> images;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductAttributeValue {
        @NotNull(message = "attribute không được để tống")
        private Long attributeId;
        @NotBlank(message = "value không được để trống")
        private String value;
    }



}
