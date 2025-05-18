package com.example.storesports.repositories;


import com.example.storesports.entity.Discount;
import com.example.storesports.entity.ProductDiscountMapping;
import org.springframework.data.jpa.repository.JpaRepository;
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



}
