package com.example.storesports.core.admin.product.payload;

import com.example.storesports.entity.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
        @NotBlank(message = "Tên sản phẩm không được để trống và null") // cái notblank thì nó validate hết
        @Length(max = 50, message = "độ dài không vượt quá ")
        private String name;
        private String description;
        private String sportType;
        private String sku;
        @NotNull(message = "nhà sản xuất không được null")
        private Long supplierId;
        @NotNull(message = "danh mục không được null")
        private Long categoryId;
        @Size(min = 1, message = "Cần ít nhất một thuộc tính")
        private List<ProductAttributeValue> productAttributeValues = new ArrayList<>();
        @Size(min = 1, message = "Cần ít nhất một biến thể sản phẩm")
        @NotNull(message = "Danh sách biến thể không được null")
        private List<ProductVariant> variants = new ArrayList<>();
        private List<Long> tagId;
        private List<MultipartFile> parentImages;
        private Boolean deleted;
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ProductAttributeValue {
            @NotNull(message = "attribute không được để null")
            private Long attributeId;
            @NotBlank(message = "value không được để trống")
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
