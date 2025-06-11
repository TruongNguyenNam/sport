package com.example.storesports.repositories;


import com.example.storesports.entity.Discount;
import com.example.storesports.entity.ProductDiscountMapping;
import com.example.storesports.infrastructure.constant.DiscountStatus;
import com.example.storesports.infrastructure.validation.RefreshTokenValid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount,Long> {

    @Query("SELECT d FROM Discount d WHERE d.endDate < :date")
    List<Discount> findExpiredDiscounts(@Param("date") LocalDateTime date);

    @Query("SELECT d FROM Discount d WHERE d.status = 'PENDING' AND d.startDate <= :now")
    List<Discount> findPendingDiscountsToActivate(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM Discount d " +
            "JOIN ProductDiscountMapping pdm ON pdm.discount = d " +
            "WHERE pdm.product.id = :productId " +
            "AND d.status = :status " +
            "AND CURRENT_TIMESTAMP BETWEEN d.startDate AND d.endDate")
    List<Discount> findActiveDiscountsByProductId(@Param("productId") Long productId,
                                                  @Param("status") DiscountStatus status);

    @Query("SELECT d FROM Discount d WHERE d.name LIKE %:name%")
    List<Discount> findByNameContaining(@Param("name") String name);


}
