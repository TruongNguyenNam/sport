package com.example.storesports.repositories;


import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage,Long> {


        List<CouponUsage> findByUserId(Long id);

        //Optional<CouponUsage> findByUserId(Long id);

        List<CouponUsage> findAllByIdIn(List<Long> ids);
        @Query(value = "SELECT cu FROM CouponUsage cu WHERE cu.user.id = :userId AND cu.deleted = false")
        List<CouponUsage> findByUserIdAndDeletedFalse(@Param("userId") Long userId);


}
