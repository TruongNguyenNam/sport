package com.example.storesports.core.admin.product.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
        private Double price;
        @NotNull(message = "Số lượng tồn kho không được để trống")
        @Min(value = 0, message = "Số lượng phải >= 0")
        private Integer stockQuantity;
        private List<MultipartFile> images;
    }


}
