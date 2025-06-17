package com.example.storesports.service.client.product.impl;

import com.example.storesports.entity.Product;
import com.example.storesports.entity.Supplier;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProductClientSpecification {

//    public static Specification<Product> findBySupplierName(String supplierName){
//        return (root,query, criteriaBuilder) -> {
//            if(supplierName != null && !supplierName.isEmpty()){
//                Join<Product, Supplier> supplierJoin = root.join("supplier", JoinType.INNER);
//                return criteriaBuilder.like(supplierJoin.get("name") , "%" + supplierName +"%");
//            }
//            return  null;
//        };
//    }




}
