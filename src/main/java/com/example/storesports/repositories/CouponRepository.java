package com.example.storesports.repositories;


import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

<<<<<<< HEAD
public interface CouponRepository extends JpaRepository<CouponUsage, Long>, JpaSpecificationExecutor<Coupon> {
=======
import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {
>>>>>>> cb30fdca92fe7403060369eeca55f150e17cc08b


//    Optional<Coupon> findByIdAndExpirationDateAfter(Long id, LocalDateTime date);

    Optional<Coupon> findByCode(String code);

}
