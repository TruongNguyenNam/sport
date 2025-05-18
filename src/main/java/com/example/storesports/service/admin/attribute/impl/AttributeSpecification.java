package com.example.storesports.service.admin.attribute.impl;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.entity.ProductAttribute;
import org.springframework.data.jpa.domain.Specification;

public class AttributeSpecification {
    public static Specification<ProductAttribute> search(String name){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                "%"+name.toLowerCase()+"%"));
    }
}
