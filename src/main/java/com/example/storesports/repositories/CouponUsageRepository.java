package com.example.storesports.repositories;


import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

        List<CouponUsage> findByUserId(Long id);

        List<CouponUsage> findAllByIdIn(List<Long> ids);

        long countByCouponId(Long couponId);

        @Query("SELECT cu FROM CouponUsage cu WHERE cu.user.id = :userId AND cu.deleted = false")
        List<CouponUsage> findByUserIdAndDeletedFalse(@Param("userId") Long userId);

        // Đếm số usage đã tặng theo couponId
        @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.deleted = false")
        long countByCouponIdAndDeletedFalse(@Param("couponId") Long couponId);

        Optional<CouponUsage> findByUserIdAndCouponIdAndDeletedFalse(Long userId, Long couponId);
}
