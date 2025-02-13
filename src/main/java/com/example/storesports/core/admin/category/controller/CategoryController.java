package com.example.storesports.core.admin.category.controller;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/admin/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
            CategoryResponse categoryResponse = categoryService.findById(id);
            return ResponseEntity.ok(categoryResponse);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<CategoryResponse> categoryResponses = categoryService.getAllCategories(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(categoryResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> findByName(@RequestParam String name) {
        List<CategoryResponse> categories = categoryService.findByName(name);
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/{id}")
    public ResponseEntity<CategoryResponse> saveOrUpdateCategory(@RequestBody CategoryRequest categoryRequest, @PathVariable Long id) {
        CategoryResponse savedCategory = categoryService.saveOrUpdateCategory(categoryRequest, id);
        return ResponseEntity.ok(savedCategory);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCategory(@RequestParam List<Long> id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
