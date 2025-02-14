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
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<CategoryResponse> categoryResponses = categoryService.getAllCategories(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(categoryResponses);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> addCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse savedCategory = categoryService.saveCategory(categoryRequest);
        return ResponseEntity.ok(savedCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @RequestBody CategoryRequest categoryRequest,
            @PathVariable Long id) {
        CategoryResponse updatedCategory = categoryService.updateCategory(categoryRequest, id);
        return ResponseEntity.ok(updatedCategory);
    }



    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
            CategoryResponse categoryResponse = categoryService.findById(id);
            return ResponseEntity.ok(categoryResponse);
    }



    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> findByName(@RequestParam String name) {
        List<CategoryResponse> categories = categoryService.findByName(name);
        return ResponseEntity.ok(categories);
    }




    @DeleteMapping
    public ResponseEntity<Void> deleteCategory(@RequestParam List<Long> id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

}
