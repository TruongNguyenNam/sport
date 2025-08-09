package com.example.storesports.service.client.product_review;

import com.example.storesports.core.client.product_review.payload.ProductReviewRequestClient;
import com.example.storesports.core.client.product_review.payload.ProductReviewResponseClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductReviewService {

    List<ProductReviewResponseClient> findByProductIdAndDeletedFalse(Long productId);
//    List<ProductReviewResponseClient> findByProductIdAndUserIdAndDeletedFalse(Long productId, Long userId);

    void addReviewProduct(ProductReviewRequestClient request, MultipartFile image) throws IOException;

    void deleted(Long id);




}
