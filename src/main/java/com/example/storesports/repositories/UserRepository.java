package com.example.storesports.repositories;


import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import com.example.storesports.infrastructure.constant.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userAddressMappings m " +
            "LEFT JOIN FETCH m.address")
    List<User> findAllWithAddresses();

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.id NOT IN (SELECT cu.user.id FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.deleted = false)")
    List<User> findCustomersNotReceivedCoupon(@Param("role") Role role, @Param("couponId") Long couponId);
}
