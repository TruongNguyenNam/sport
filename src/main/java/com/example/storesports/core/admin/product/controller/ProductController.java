package com.example.storesports.core.admin.product.controller;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.entity.Product;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/admin/product")
@Validated
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

//    @GetMapping
//    public ResponseEntity<Page<ProductResponse>> getAllProducts(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//        Page<ProductResponse> productResponses = productService.getAllProducts(page, size);
//        return ResponseEntity.ok(productResponses);
//    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<ProductResponse> productResponses = productService.getAllProducts(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(productResponses);
        return ResponseEntity.ok(response);
    }

}
