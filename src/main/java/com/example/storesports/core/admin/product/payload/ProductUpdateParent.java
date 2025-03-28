package com.example.storesports.core.admin.product.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateParent {

    private String name;
    private String description;
    private String sportType;
    private String sku;
    private Long supplierId;
    private Long categoryId;
    private List<Long> tagId;
    private List<MultipartFile> parentImages;

}
