package com.example.storesports.core.admin.tag.controller;

import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.core.admin.tag.payload.ProductTagRequest;
import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import com.example.storesports.entity.ProductTag;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.supplier.SupplierService;
import com.example.storesports.service.admin.tag.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/{id}")
    public ResponseEntity<ProductTagResponse> getTagById(@RequestParam Long id) {
        ProductTagResponse response = productTagService.findById(id);
        return ResponseEntity.ok(response);
    }

    // Thêm mới hoặc cập nhật ProductTag
    @PostMapping
    public ResponseEntity<ProductTagResponse> saveOrUpdateTag(
            @RequestBody ProductTagRequest productTagRequest,
            @RequestParam(required = false) Long id) {
        ProductTagResponse response = productTagService.saveOrUpdateTag(productTagRequest, id);
        return ResponseEntity.ok(response);
    }

    // Xóa nhiều ProductTag theo danh sách ID
    @DeleteMapping
    public ResponseEntity<Void> deleteTags(@RequestParam List<Long> ids) {
        productTagService.deleteTag(ids);
        return ResponseEntity.noContent().build();
    }




}
