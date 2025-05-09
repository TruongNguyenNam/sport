package com.example.storesports.repositories;


import com.example.storesports.entity.OrderItem;
import com.example.storesports.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    @Query("from OrderItem a where a.order.id = :id")
    List<OrderItem> findByOrderId(@Param("id") Long id);


}
