package com.example.storesports.service.admin.attribute;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import org.springframework.data.domain.Page;

public interface AttributeService {

    Page<ProductAttributeResponse> getAllProductAttribute(int size, int page);



}
