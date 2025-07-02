package com.example.storesports.core.admin.supplier.controller;

import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.supplier.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseData<List<SupplierResponse>> getAllSupplier() {
        List<SupplierResponse> categories = supplierService.findAllSupplier();
        return ResponseData.<List<SupplierResponse>>builder()
                .status(HttpStatus.OK.value())
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

    @PostMapping("/add")
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
