package com.example.storesports.repositories;


import com.example.storesports.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue,Long> {


    @Query("from ProductAttributeValue a where a.product.id = :id")
    List<ProductAttributeValue> findByProductId(@Param("id") Long id);

    @Modifying
    @Query("delete from ProductAttributeValue a where a.product.id = :id")
    void deleteByProductId(@Param("id") Long id);

}
