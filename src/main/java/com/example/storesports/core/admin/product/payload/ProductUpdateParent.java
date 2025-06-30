package com.example.storesports.core.admin.product.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateParent {
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
    private List<Long> tagId;
    private List<MultipartFile> parentImages;

}
