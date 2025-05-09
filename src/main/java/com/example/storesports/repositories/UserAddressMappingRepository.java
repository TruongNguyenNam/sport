package com.example.storesports.repositories;


import com.example.storesports.entity.Payment;
import com.example.storesports.entity.UserAddressMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAddressMappingRepository extends JpaRepository<UserAddressMapping,Long> {

    @Query("select p from UserAddressMapping p where p.user.id = :id")
    Optional<UserAddressMapping> findByUserId(@Param("id") Long id);
//    List<UserAddressMapping> findByUserId(Long userId);
    Optional<UserAddressMapping> findByUserIdAndAddressId(Long userId, Long addressId);
}
