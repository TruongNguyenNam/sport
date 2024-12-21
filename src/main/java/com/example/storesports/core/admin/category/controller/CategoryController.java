package com.example.storesports.core.admin.category.controller;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/v1/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<CategoryResponse> categoryResponses = categoryService.getAllCategories(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(categoryResponses);
        return ResponseEntity.ok(response);
    }

}
