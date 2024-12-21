package com.example.storesports.service.admin.supplier;

import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import org.springframework.data.domain.Page;

public interface SupplierService {

    Page<SupplierResponse> getAllSupplier(int size,int page);


}
