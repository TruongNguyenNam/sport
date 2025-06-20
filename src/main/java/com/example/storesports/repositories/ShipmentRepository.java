package com.example.storesports.repositories;


import com.example.storesports.entity.Order;
import com.example.storesports.entity.Shipment;
import com.example.storesports.entity.UserAddressMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {

    @Query("select p from Shipment p where p.order.id = :id and p.deleted = false")
    Optional<Shipment> findByOrderId(@Param("id") Long id);

//    @Query("select p from Shipment p where p.order.id = :id")
//    Optional<Shipment> findByOrder(@Param("id") Long id);
//    Optional<Shipment> findByCarrierAndOrder(String carrier, Order order);


}
