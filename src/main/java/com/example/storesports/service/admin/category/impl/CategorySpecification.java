package com.example.storesports.service.admin.category.impl;

import com.example.storesports.entity.Category;
import com.example.storesports.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CategorySpecification {
    public static Specification<Category> findByName(String name) {
        return (root, query, criteriaBuilder) ->
                StringUtils.hasText(name) ? criteriaBuilder.like(root.get("name"), "%" + name + "%") : null;
    }


}
