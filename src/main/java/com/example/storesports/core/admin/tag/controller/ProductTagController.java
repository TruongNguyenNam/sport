package com.example.storesports.core.admin.tag.controller;

import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import com.example.storesports.entity.ProductTag;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.supplier.SupplierService;
import com.example.storesports.service.admin.tag.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/productTag")
@RequiredArgsConstructor
public class ProductTagController {

    private final ProductTagService productTagService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<ProductTagResponse> productTagResponses = productTagService.getAllTags(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(productTagResponses);
        return ResponseEntity.ok(response);
    }




}
