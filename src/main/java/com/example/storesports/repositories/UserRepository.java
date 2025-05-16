package com.example.storesports.repositories;


import com.example.storesports.entity.Token;
import com.example.storesports.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userAddressMappings m " +
            "LEFT JOIN FETCH m.address")
    List<User> findAllWithAddresses();

}
