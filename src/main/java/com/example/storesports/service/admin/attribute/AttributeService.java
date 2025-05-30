package com.example.storesports.service.admin.attribute;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeRequest;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import org.springframework.data.domain.Page;

import javax.management.Attribute;
import java.util.List;

public interface AttributeService {

   // Page<ProductAttributeResponse> getAllProductAttribute(int size, int page);

    List<ProductAttributeResponse> findAllProductAttribute();
    ProductAttributeResponse saveOrUpdateAttribute(ProductAttributeRequest productAttributeRequest, Long id);


    void deleteAttribute(List<Long> id);

    ProductAttributeResponse save(ProductAttributeRequest productAttributeRequest);

    ProductAttributeResponse update(Long id,ProductAttributeRequest productAttributeRequest);

    ProductAttributeResponse findById(Long id);
    List<ProductAttributeResponse> searchName(String name);
}
