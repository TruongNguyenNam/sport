package com.example.storesports.core.admin.tag.controller;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.core.admin.tag.payload.ProductTagRequest;
import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import com.example.storesports.entity.ProductTag;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.supplier.SupplierService;
import com.example.storesports.service.admin.tag.ProductTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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
    public ResponseData<List<ProductTagResponse>> getAllTags(){
        List<ProductTagResponse> productTagResponses = productTagService.findAllTags();
        return ResponseData.<List<ProductTagResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách nhãn thành công")
                .data(productTagResponses)
                .build();
    }

    @GetMapping("/{id}")
    public ResponseData<ProductTagResponse> getTagById(@PathVariable Long id) {
        ProductTagResponse response = productTagService.findById(id);
        return ResponseData.<ProductTagResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin thẻ sản phẩm thành công")
                .data(response)
                .build();
    }

    // Thêm mới hoặc cập nhật ProductTag
    @PostMapping("/add")
    public ResponseData<ProductTagResponse> addProductTag(@RequestBody ProductTagRequest tagRequest) {
        ProductTagResponse savedTag = productTagService.saveTag(tagRequest);
        return ResponseData.<ProductTagResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Thêm thẻ sản phẩm thành công")
                .data(savedTag)
                .build();
    }
    // Cập nhật ProductTag
    @PutMapping("/update/{id}")
    public ResponseData<ProductTagResponse> updateProductTag(
            @RequestBody ProductTagRequest productTagRequest,
            @PathVariable Long id) {
        ProductTagResponse updatedTag = productTagService.updateTag(productTagRequest, id);
        return ResponseData.<ProductTagResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật thẻ sản phẩm thành công")
                .data(updatedTag)
                .build();
    }

    // Xóa nhiều ProductTag theo danh sách ID
    @DeleteMapping
    public ResponseEntity<Void> deleteTags(@RequestParam List<Long> ids) {
        productTagService.deleteTag(ids);
        return ResponseEntity.noContent().build();
    }


//    @GetMapping
//    public ResponseEntity<Map<String, Object>> getAllTags(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "2") int size) {
//        Page<ProductTagResponse> productTagResponses = productTagService.getAllTags(page, size);
//        Map<String, Object> response = PageUtils.createPageResponse(productTagResponses);
//        return ResponseEntity.ok(response);
//    }



}
