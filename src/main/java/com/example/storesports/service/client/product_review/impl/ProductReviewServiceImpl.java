package com.example.storesports.service.client.product_review.impl;

import com.example.storesports.core.client.product_review.payload.ProductReviewRequestClient;
import com.example.storesports.core.client.product_review.payload.ProductReviewResponseClient;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductReview;
import com.example.storesports.entity.User;
import com.example.storesports.repositories.ProductRepository;
import com.example.storesports.repositories.ProductReviewRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.admin.image.cloudinary.CloudinaryService;
import com.example.storesports.service.client.product_review.ProductReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductReviewServiceImpl implements ProductReviewService {
    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<ProductReviewResponseClient> findByProductIdAndDeletedFalse(Long productId) {
        List<ProductReview> reviews = productReviewRepository.findByParentProductIdAndDeletedFalse(productId);
        return reviews.stream()
                .map(this::mapToResponseClient)
                .collect(Collectors.toList());
    }


    @Override
    public void addReviewProduct(ProductReviewRequestClient request, MultipartFile image) throws IOException {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setDeleted(false);

        // Upload hình ảnh lên Cloudinary nếu có
        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadFile(image, "product_reviews");
            review.setImageUrl(imageUrl);
        } else {
            review.setImageUrl(request.getImageUrl());
        }

        productReviewRepository.save(review);
    }

    @Override
    public void deleted(Long id) {
        ProductReview review = productReviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setDeleted(true);
        productReviewRepository.save(review);
    }

    private ProductReviewResponseClient mapToResponseClient(ProductReview review) {
        ProductReviewResponseClient response = new ProductReviewResponseClient();
        response.setId(review.getId());
        response.setProductName(review.getProduct().getName());
        response.setUserName(review.getUser().getUsername());
        response.setRating(review.getRating());
        response.setImageUrl(review.getImageUrl());
        response.setReviewText(review.getReviewText());
        response.setDeleted(review.getDeleted());
        return response;
    }



}
