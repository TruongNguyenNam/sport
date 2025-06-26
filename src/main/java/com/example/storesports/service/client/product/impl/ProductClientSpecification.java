package com.example.storesports.service.client.product.impl;

import com.example.storesports.entity.Category;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.Supplier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class ProductClientSpecification {

    public static Specification<Product> filterProducts(
            String categoryName,
            String sportType,
            Double minPrice,
            Double maxPrice
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Join với bảng Category
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(categoryJoin.get("name")),
                        "%" + categoryName.toLowerCase() + "%"
                ));
            }

            // Lọc theo sportType
            if (sportType != null && !sportType.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("sportType")),
                        "%" + sportType.toLowerCase() + "%"
                ));
            }

            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));
//            predicates.add(criteriaBuilder.isNotNull(root.get("parentProductId")));


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }





}
