package com.example.storesports.core.admin.image.controller;
import com.example.storesports.core.admin.image.payload.ProductImageResponse;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.service.admin.image.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/ProductImage")
@RequiredArgsConstructor
public class ProductImageController {
//    @Autowired
      private final ProductImageService productImageService;


    @PostMapping("/upload")
    public ResponseEntity<List<Long>> uploadProductImages(@RequestParam("images") List<MultipartFile> images) {
        try {
            List<Long> imageIds = productImageService.saveProductImage(images);
            return ResponseEntity.ok(imageIds);
        } catch (Exception e) {
            throw new ErrorException("Failed to upload images");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductImageResponse> getProductImageById(@PathVariable Long id) {
        ProductImageResponse response = productImageService.getPictureById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductImageById(@PathVariable Long id) {
        productImageService.deleteById(id);
        return ResponseEntity.noContent().build();  // HTTP 204 No Content
    }


}
