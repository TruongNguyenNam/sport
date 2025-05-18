package com.example.storesports.core.admin.category.controller;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/admin/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;


    @GetMapping()
    public ResponseData<List<CategoryResponse>> getAllCategories(){
        List<CategoryResponse> categories = categoryService.findAllCategories();
        return ResponseData.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value()) // log
                .message("lấy danh sách danh mục thành công") //
                .data(categories)
                .build();
    }

    @PostMapping("/add")
    public ResponseData<CategoryResponse> addCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse savedCategory = categoryService.saveCategory(categoryRequest);
        return ResponseData.<CategoryResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Thêm danh mục thành công")
                .data(savedCategory)
                .build();
    }


    @PutMapping("/update/{id}")
    public ResponseData<CategoryResponse> updateCategory(
            @RequestBody CategoryRequest categoryRequest,
            @PathVariable Long id) {
        CategoryResponse updatedCategory = categoryService.updateCategory(categoryRequest, id);
        return ResponseData.<CategoryResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật danh mục thành công")
                .data(updatedCategory)
                .build();
    }


    @GetMapping("/{id}")
    public ResponseData<CategoryResponse> getCategoryById(@PathVariable Long id) {
        CategoryResponse categoryResponse = categoryService.findById(id);
        return ResponseData.<CategoryResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin danh mục thành công")
                .data(categoryResponse)
                .build();
    }



    @GetMapping("/search")
    public ResponseData<List<CategoryResponse>> findByName(@RequestParam String name) {
        List<CategoryResponse> categories = categoryService.findByName(name);
        return ResponseData.<List<CategoryResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Tìm kiếm danh mục thành công")
                .data(categories)
                .build();
    }

    @DeleteMapping("/delete")
    public ResponseData<Void> deleteCategory(@RequestParam List<Long> ids) {
        categoryService.deleteCategory(ids);
        return ResponseData.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Xóa danh mục thành công")
                .build();
    }



    //    @GetMapping
//    public ResponseEntity<Map<String, Object>> getAllCategories(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "2") int size) {
//        Page<CategoryResponse> categoryResponses = categoryService.getAllCategories(page, size);
//        Map<String, Object> response = PageUtils.createPageResponse(categoryResponses);
//        return ResponseEntity.ok(response);
//    }


}
