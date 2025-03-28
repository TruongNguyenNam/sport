package com.example.storesports.core.admin.supplier.controller;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.entity.Supplier;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.supplier.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/{id}")
    public ResponseData<SupplierResponse> getSupplierById(@PathVariable Long id) {
        SupplierResponse response = supplierService.findById(id);
        return ResponseData.<SupplierResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy thông tin nhà cung cấp thành công")
                .data(response)
                .build();
    }

    @GetMapping
    public ResponseData<List<SupplierResponse>> getAllSupplier(){
        List<SupplierResponse> categories = supplierService.findAllSupplier();
        return ResponseData.<List<SupplierResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách nhà cung cấp thành công")
                .data(categories)
                .build();
    }

    @GetMapping("/search")
    public ResponseData<List<SupplierResponse>> findByName(@RequestParam String name) {
        List<SupplierResponse> suppliers = supplierService.findByName(name);
        return ResponseData.<List<SupplierResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Tìm kiếm nhà cung cấp thành công")
                .data(suppliers)
                .build();
    }

    @PutMapping("/{id}")
    public ResponseData<SupplierResponse> updateSupplier(@RequestBody SupplierRequest supplierRequest, @PathVariable Long id) {
        SupplierResponse updatedSupplier = supplierService.updateSupplier(supplierRequest, id);
        return ResponseData.<SupplierResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật nhà cung cấp thành công")
                .data(updatedSupplier)
                .build();
    }

    @PostMapping
    public ResponseData<SupplierResponse> saveSupplier(@RequestBody SupplierRequest supplierRequest) {
        SupplierResponse response = supplierService.saveSupplier(supplierRequest);
        return ResponseData.<SupplierResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Thêm nhà cung cấp thành công")
                .data(response)
                .build();
    }

    @DeleteMapping
    public ResponseData<Void> deleteSupplier(@RequestParam List<Long> id) {
        supplierService.deleteSupplier(id);
        return ResponseData.<Void>builder()
                .status(HttpStatus.NO_CONTENT.value())
                .message("Xóa nhà cung cấp thành công")
                .build();
    }
    
}
