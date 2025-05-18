package com.example.storesports.repositories;

import com.example.storesports.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, JpaSpecificationExecutor<Coupon> {


//    Optional<Coupon> findByIdAndExpirationDateAfter(Long id, LocalDateTime date);

    Optional<Coupon> findByCode(String code);

<<<<<<< HEAD
}
=======
}
>>>>>>> e7231ef37979ba6d569c27bd5ff42f00fcbd66c0
