package com.example.storesports.service.admin.supplier;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SupplierService {

  //  Page<SupplierResponse> getAllSupplier(int size,int page);

    List<SupplierResponse> findAllSupplier();
    SupplierResponse saveSupplier(SupplierRequest supplierRequest);

    SupplierResponse updateSupplier(SupplierRequest supplierRequest, Long id);

    List<SupplierResponse> findByName(String name);

    void deleteSupplier(List<Long> id);


    SupplierResponse findById(Long id);

}
