package com.example.storesports.repositories;


import com.example.storesports.entity.ProductImage;
import com.example.storesports.entity.ProductSportTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSportTypeMappingRepository extends JpaRepository<ProductSportTypeMapping,Long> {
    @Query("from ProductSportTypeMapping a where a.product.id = :id")
    List<ProductSportTypeMapping> findByProductId(@Param("id") Long id);




}
