package com.example.storesports.service.admin.product_attribute_value;

import com.example.storesports.core.admin.attribute_value.payload.ProductAttributeValueResponse;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.entity.ProductAttributeValue;

import java.util.List;

public interface ProductAttributeValueService {

    List<ProductAttributeValueResponse> getAll(Long parentId);

}
