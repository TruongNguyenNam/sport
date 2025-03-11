package com.example.storesports.repositories;


import com.example.storesports.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage,Long> {

    @Query("from ProductImage a where a.product.id = :id")
    List<ProductImage> findByProductId(@Param("id") Long id);

    //update
    @Modifying
    @Query("delete from ProductImage a where a.product.id = :id")
    void  deleteByProductId(@Param("id") Long id);

    boolean existsByProductId(Long productId);

}
