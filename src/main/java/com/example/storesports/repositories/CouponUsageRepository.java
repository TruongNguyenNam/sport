package com.example.storesports.repositories;


import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponUsageRepository extends JpaRepository<CouponUsage,Long> {


        List<CouponUsage> findByUserId(Long id);

}
