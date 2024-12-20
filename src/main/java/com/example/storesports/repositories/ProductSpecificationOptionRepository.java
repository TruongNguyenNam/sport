package com.example.storesports.repositories;


import com.example.storesports.entity.ProductSpecificationOption;
import com.example.storesports.entity.ProductSportTypeMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductSpecificationOptionRepository extends JpaRepository<ProductSpecificationOption,Long> {


    @Query("from ProductSpecificationOption a where a.product.id = :id")
    List<ProductSpecificationOption> findByProductId(@Param("id") Long id);


    @Modifying
    @Query("delete from ProductSpecificationOption a where a.product.id = :id")
    void deleteByProductId(@Param("id") Long id);

}
