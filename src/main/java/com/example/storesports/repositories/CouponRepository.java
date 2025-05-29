package com.example.storesports.repositories;

import com.example.storesports.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {


//    Optional<Coupon> findByIdAndExpirationDateAfter(Long id, LocalDateTime date);

    Optional<Coupon> findByCode(String code);

    @Query("SELECT c \n" +
            "FROM Coupon c \n" +
            "WHERE c.deleted = false \n" +
            "  AND c.quantity > 0 order by c.id desc")
    List<Coupon> getAllCoupon();

}
