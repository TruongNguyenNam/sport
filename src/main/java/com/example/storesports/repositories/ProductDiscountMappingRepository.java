package com.example.storesports.repositories;


import com.example.storesports.entity.Discount;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductDiscountMapping;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDiscountMappingRepository extends JpaRepository<ProductDiscountMapping,Long> {

    @Query("SELECT pdm FROM ProductDiscountMapping pdm WHERE pdm.discount = :discount")
    List<ProductDiscountMapping> findByDiscount(@Param("discount") Discount discount);

    @Query("select count (p)from ProductDiscountMapping p where p.discount.id=:discount")
    int countByDiscount(@Param("discount") Long discount);

    @Query("SELECT COUNT(m) > 0 FROM ProductDiscountMapping m WHERE m.product = :product AND m.discount = :discount")
    boolean existsByProductAndDiscount(@Param("product") Product product, @Param("discount") Discount discount);

    @Query("select pdm from ProductDiscountMapping pdm where pdm.discount.id=:discountId")
    List<ProductDiscountMapping> findByDiscountId(@Param("discountId") Long discountId);

    @Modifying
    @Transactional
    @Query ("delete from ProductDiscountMapping pdm where pdm.discount=:discount")
    void deleteByDiscount(@Param("discount") Discount discount);
}
