package com.example.storesports.service.admin.supplier.impl;

import com.example.storesports.entity.Category;
import com.example.storesports.entity.Supplier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SupplierSpecification {
    public static Specification<Supplier> findByName(String name) {
        return (root, query, criteriaBuilder) ->
                StringUtils.hasText(name) ? criteriaBuilder.like(root.get("name"), "%" + name + "%") : null;
    }
}
