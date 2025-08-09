package com.example.storesports.core.client.product_review.controller;

import com.example.storesports.core.client.product.payload.ProductResponseClient;
import com.example.storesports.core.client.product_review.payload.ProductReviewRequestClient;
import com.example.storesports.core.client.product_review.payload.ProductReviewResponseClient;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.product_review.ProductReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/client/product_review")
@Validated
@RequiredArgsConstructor
@Tag(name = "ProductReview", description = "Endpoints for managing products")
@Slf4j
public class ProductReviewController {

    private final ProductReviewService productReviewService;

    @GetMapping("/product/{ParentProductId}/review")
    public ResponseData<List<ProductReviewResponseClient>> getReviewsByParentProductId(@PathVariable Long ParentProductId) {
        List<ProductReviewResponseClient> reviews = productReviewService.findByProductIdAndDeletedFalse(ParentProductId);
        return ResponseData.<List<ProductReviewResponseClient>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đánh giá sản phẩm thành công")
                .data(reviews)
                .build();
    }

    @PostMapping
    public ResponseData<Void> addReview(
            @RequestPart("review") ProductReviewRequestClient request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        productReviewService.addReviewProduct(request, image);
        return ResponseData.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Thêm đánh giá sản phẩm thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteReview(@PathVariable Long id) {
        productReviewService.deleted(id);
        return ResponseData.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Xóa đánh giá sản phẩm thành công")
                .build();
    }


}
