package com.example.storesports.repositories;


import com.example.storesports.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute,Long> {

    @Query("select p from  ProductAttribute p order by p.id desc ")
    List<ProductAttribute> findAllProductAttribute();

}
