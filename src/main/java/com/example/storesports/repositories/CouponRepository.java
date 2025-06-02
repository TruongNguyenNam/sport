package com.example.storesports.repositories;

import com.example.storesports.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {

    boolean existsByCodeCoupon(String codeCoupon);

    @Query("SELECT c FROM Coupon c WHERE c.codeCoupon LIKE %:codeCoupon% ORDER BY c.id DESC")
    List<Coupon> findByCodeCoupon(@Param("codeCoupon") String codeCoupon);

    @Query("SELECT c FROM Coupon c WHERE c.deleted = false AND c.couponStatus = 'ACTIVE' ORDER BY c.id desc")
    List<Coupon> getAllActiveCoupons();

}