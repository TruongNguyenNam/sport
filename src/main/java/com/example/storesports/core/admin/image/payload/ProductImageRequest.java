package com.example.storesports.core.admin.image.payload;

import com.example.storesports.entity.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductImageRequest {

    private Long productId;

    private String imageUrl;
}
