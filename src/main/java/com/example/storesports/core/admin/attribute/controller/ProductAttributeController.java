package com.example.storesports.core.admin.attribute.controller;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeRequest;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.attribute.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/admin/attribute")
@RequiredArgsConstructor
public class ProductAttributeController {

        private final AttributeService attributeService;

        @GetMapping
        public ResponseEntity<Map<String, Object>> getAllProductAttribute(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "2") int size) {
            Page<ProductAttributeResponse> productSpecificationResponses = attributeService.getAllProductAttribute(page, size);
            Map<String, Object> response = PageUtils.createPageResponse(productSpecificationResponses);
            return ResponseEntity.ok(response);
        }

    @PostMapping("/{id}")
    public ResponseEntity<ProductAttributeResponse> saveOrUpdateAttribute(@RequestBody ProductAttributeRequest productAttributeRequest, @PathVariable Long id) {
        ProductAttributeResponse savedAttribute = attributeService.saveOrUpdateAttribute(productAttributeRequest, id);
        return ResponseEntity.ok(savedAttribute);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAttribute(@RequestParam List<Long> id) {
        attributeService.deleteAttribute(id);
        return ResponseEntity.noContent().build();
    }


}
