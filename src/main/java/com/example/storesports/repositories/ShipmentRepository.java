package com.example.storesports.repositories;


import com.example.storesports.entity.Shipment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {

    @Query("select p from Shipment p where p.order.id = :id and p.deleted = false")
    Optional<Shipment> findByOrderId(@Param("id") Long id);

//    @Query("select p from Shipment p where p.order.id = :orderId and p.deleted = false")
//    List<Shipment> findByOrderId(@Param("orderId") Long orderId);

//    @Transactional
//    @Modifying
//    @Query("DELETE FROM ShipmentItem si WHERE si.orderItem.order.id = :orderId")
//    void deleteByOrderId(Long orderId);


    @Query("select p from Shipment p where p.order.id = :id and p.deleted = false")
    List<Shipment> findAllByOrderId(@Param("id") Long id);
    @Transactional
    @Modifying
    @Query("DELETE FROM Shipment p WHERE p.order.id = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);

//    @Query("select p from Shipment p where p.order.id = :id and p.deleted = false")
//    List<Shipment> findByOrderIds(@Param("id") Long id);

//    @Query("select p from Shipment p where p.order.id = :id")
//    Optional<Shipment> findByOrder(@Param("id") Long id);
//    Optional<Shipment> findByCarrierAndOrder(String carrier, Order order);


}
