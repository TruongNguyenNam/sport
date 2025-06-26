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
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    // check trung
    Boolean existsByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN u.userAddressMappings m " +
            "LEFT JOIN m.address where u.role = 'CUSTOMER' ORDER BY u.id desc ")
    List<User> findAllWithAddresses();

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.id NOT IN (SELECT cu.user.id FROM CouponUsage cu WHERE cu.coupon.id = :couponId AND cu.deleted = false) ORDER BY u.id desc ")
    List<User> findCustomersNotReceivedCoupon(@Param("role") Role role, @Param("couponId") Long couponId);

    List<User> findByEmailOrderByIdDesc(String email);

    List<User> findByPhoneNumberOrderByIdDesc(String phoneNumber);

    List<User> findByUsernameContainingIgnoreCaseOrderByIdDesc(String username);


}
