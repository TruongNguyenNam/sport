package com.example.storesports.service.admin.attribute;

import com.example.storesports.core.admin.attribute.payload.ProductSpecificationResponse;
import com.example.storesports.entity.ProductSpecification;
import org.springframework.data.domain.Page;

public interface AttributeService {

    Page<ProductSpecificationResponse> getAllProductAttribute(int size, int page);



}
