package com.example.storesports.service.admin.attribute;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeRequest;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface AttributeService {

    Page<ProductAttributeResponse> getAllProductAttribute(int size, int page);


    ProductAttributeResponse saveOrUpdateAttribute(ProductAttributeRequest productAttributeRequest, Long id);

    //List<SupplierResponse> findByName(String name);

    void deleteAttribute(List<Long> id);

}
