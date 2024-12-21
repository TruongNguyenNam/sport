package com.example.storesports.core.admin.supplier.controller;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.entity.Supplier;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.supplier.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSupplier(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<SupplierResponse> supplierResponses = supplierService.getAllSupplier(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(supplierResponses);
        return ResponseEntity.ok(response);
    }


}
