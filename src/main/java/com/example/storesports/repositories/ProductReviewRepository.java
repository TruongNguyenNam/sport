package com.example.storesports.repositories;


import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview,Long> {

    @Query("SELECT pr FROM ProductReview pr JOIN pr.product p WHERE (p.id = :parentProductId OR p.parentProductId = :parentProductId) AND pr.deleted = false ORDER BY pr.id DESC")
    List<ProductReview> findByParentProductIdAndDeletedFalse(@Param("parentProductId") Long parentProductId);
}
