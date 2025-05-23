package com.example.storesports.core.admin.attribute.controller;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeRequest;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.infrastructure.exceptions.DuplicateEntityException;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.attribute.AttributeService;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/attribute")
@RequiredArgsConstructor
public class ProductAttributeController {
        private final AttributeService attributeService;

    @GetMapping("/{id}")
    public ResponseData<ProductAttributeResponse> getProductAttributeById(@PathVariable Long id) {
        ProductAttributeResponse response = attributeService.findById(id);
        return ResponseData.<ProductAttributeResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin thuộc tính sản phẩm thành công")
                .data(response)
                .build();
    }

        @GetMapping
        public ResponseData<List<ProductAttributeResponse>> getAllProductAttribute(){
            List<ProductAttributeResponse> productAttributeResponses = attributeService.findAllProductAttribute();
            return ResponseData.<List<ProductAttributeResponse>>builder()
                    .status(HttpStatus.OK.value())
                    .message("lấy danh sách thuộc tính thành công")
                    .data(productAttributeResponses)
                    .build();
        }


    @PostMapping("/save")
    public ResponseData<ProductAttributeResponse> save(@Valid @RequestBody ProductAttributeRequest productAttributeRequest) {
        try {
            ProductAttributeResponse productAttributeResponse = attributeService.save(productAttributeRequest);
            return ResponseData.<ProductAttributeResponse>builder()
                    .status(200)
                    .message("Tạo thành công")
                    .data(productAttributeResponse)
                    .build();
        } catch (DuplicateEntityException ex) {
            return ResponseData.<ProductAttributeResponse>builder()
                    .status(400)
                    .message("Lỗi: " + ex.getMessage())
                    .data(null)
                    .build();
        } catch (Exception ex) {
            return ResponseData.<ProductAttributeResponse>builder()
                    .status(500)
                    .message("Lỗi hệ thống: " + ex.getMessage())
                    .data(null)
                    .build();
        }
    }


    @PutMapping("/update/{id}")
        public ResponseData<ProductAttributeResponse> update(@PathVariable("id") Long id,@Valid @RequestBody ProductAttributeRequest productAttributeRequest) {

        try {
            ProductAttributeResponse productAttributeResponse = attributeService.update(id, productAttributeRequest);
            return ResponseData.<ProductAttributeResponse>builder()
                    .status(200)
                    .message("update thành công")
                    .data(productAttributeResponse)
                    .build();
        } catch (DuplicateEntityException ex) {
            return ResponseData.<ProductAttributeResponse>builder()
                    .status(400)
                    .message("Lỗi: " + ex.getMessage())
                    .data(null)
                    .build();
        } catch (Exception ex) {
            return ResponseData.<ProductAttributeResponse>builder()
                    .status(500)
                    .message("Lỗi hệ thống: " + ex.getMessage())
                    .data(null)
                    .build();
        }
    }

    @GetMapping("/search/name/{name}")
    public ResponseData<List<ProductAttributeResponse>> searchName(@PathVariable("name") String name){

        List<ProductAttributeResponse> attributeResponses = attributeService.searchName(name);
        return ResponseData.<List<ProductAttributeResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Tìm kiếm thuộc tính thành công")
                .data(attributeResponses)
                .build();
    }


//        @PostMapping("/save")
//        public ResponseData<ProductAttributeResponse> save(@RequestBody ProductAttributeRequest productAttributeRequest){
//        ProductAttributeResponse productAttributeResponse=attributeService.save(productAttributeRequest);
//        return ResponseData.<ProductAttributeResponse>builder().status(200)
//                .message("tạo thành công")
//                .data(productAttributeResponse)
//                .build();
//        }
//
//        @PutMapping("/update/{id}")
//        public ResponseData<ProductAttributeResponse> update(@PathVariable("id") Long id, @RequestBody ProductAttributeRequest productAttributeRequest) {
//        ProductAttributeResponse productAttributeResponse=attributeService.update(id, productAttributeRequest);
//        return ResponseData.<ProductAttributeResponse>builder().data(productAttributeResponse)
//                .message("update thành công")
//                .status(200).build();
//
//    }
//            @PostMapping("/{id}")
//    public ResponseEntity<ProductAttributeResponse> saveOrUpdateAttribute(@RequestBody ProductAttributeRequest productAttributeRequest, @PathVariable Long id) {
//        ProductAttributeResponse savedAttribute = attributeService.saveOrUpdateAttribute(productAttributeRequest, id);
//        return ResponseEntity.ok(savedAttribute);
//    }
//
//    @DeleteMapping
//    public ResponseEntity<Void> deleteAttribute(@RequestParam List<Long> id) {
//        attributeService.deleteAttribute(id);
//        return ResponseEntity.noContent().build();
//    }



}
