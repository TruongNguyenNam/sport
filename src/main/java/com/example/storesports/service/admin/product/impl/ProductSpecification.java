package com.example.storesports.service.admin.product.impl;

import com.example.storesports.entity.Category;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.Supplier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class ProductSpecification {

    public static Specification<Product> findByName(String name) {
        return (root, query, criteriaBuilder) ->
                StringUtils.hasText(name) ? criteriaBuilder.like(root.get("name"), "%" + name + "%") : null;
    }

    public static Specification<Product> findBySportType(String sportType){
        return (root, query, criteriaBuilder) ->
                StringUtils.hasText(sportType) ? criteriaBuilder.like(root.get("sportType"),"%" + sportType + "%") : null;
    }


    public static Specification<Product> findByCategoryName(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            if (categoryName != null && !categoryName.isEmpty()) {
                Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
                return criteriaBuilder.like(categoryJoin.get("name"), "%" + categoryName + "%");
            }
            return null;
        };
    }

    public static Specification<Product> findBySupplierName(String supplierName){
        return (root,query, criteriaBuilder) -> {
            if(supplierName != null && !supplierName.isEmpty()){
                Join<Product, Supplier> supplierJoin = root.join("supplier",JoinType.INNER);
                return criteriaBuilder.like(supplierJoin.get("name") , "%" + supplierName +"%");
            }
            return  null;
        };
    }

    public static Specification<Product> hasPriceRange(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else if (maxPrice != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
            return null;
        };
    }


}
