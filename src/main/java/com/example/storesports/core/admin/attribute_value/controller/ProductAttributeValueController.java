package com.example.storesports.core.admin.attribute_value.controller;

import com.example.storesports.core.admin.attribute_value.payload.ProductAttributeValueResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.product_attribute_value.ProductAttributeValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/attribute_value")
@RequiredArgsConstructor
public class ProductAttributeValueController {

    private final ProductAttributeValueService productAttributeValueService;


    @GetMapping("/{parentId}")
    public ResponseData<List<ProductAttributeValueResponse>> getFirstVariantAttributes(@PathVariable Long parentId) {
        List<ProductAttributeValueResponse> responses = productAttributeValueService.getAll(parentId);

        return ResponseData.<List<ProductAttributeValueResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thuộc tính của biến thể đầu tiên thành công")
                .data(responses)
                .build();
    }



}
