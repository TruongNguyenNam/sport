package com.example.storesports.repositories;


import com.example.storesports.entity.ProductTag;
import com.example.storesports.entity.ProductTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductTagMappingRepository  extends JpaRepository<ProductTagMapping,Long> {

    //@Query("from ProductTagMapping i where i.product.id = : productId")
    @Query("FROM ProductTagMapping i WHERE i.product.id = :productId")
    List<ProductTagMapping> findByProductId(@Param("productId") Long id);

    @Modifying
    @Query("delete from ProductTagMapping p where p.product.id = :id")
    void deleteByProductId(@Param("id") Long id);




}
