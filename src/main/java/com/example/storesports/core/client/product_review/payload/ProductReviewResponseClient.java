package com.example.storesports.core.client.product_review.payload;

import com.example.storesports.entity.Product;
import com.example.storesports.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductReviewResponseClient {


    private Long id;

    private String productName;

    private String userName;

    private Integer rating;

    private String imageUrl;

    private String reviewText;

    private Boolean deleted;

}
