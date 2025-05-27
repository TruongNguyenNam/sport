package com.example.storesports.repositories;


import com.example.storesports.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long>, JpaSpecificationExecutor<Order> {

    @Query("SELECT o FROM Order o WHERE o.orderCode = :key")
    Optional<Order> findByOrderCode(@Param("key") String key);


    @Query("select p from Order p where p.isPos = false")
    List<Order> findAllOrderIsPos();

}
