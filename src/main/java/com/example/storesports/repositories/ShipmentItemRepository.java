package com.example.storesports.repositories;


import com.example.storesports.entity.ShipmentItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ShipmentItemRepository  extends JpaRepository<ShipmentItem,Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM ShipmentItem si WHERE si.orderItem.order.id = :orderId")
    void deleteByOrderId(Long orderId);


    @Transactional
    @Modifying
    @Query("delete from ShipmentItem s where s.orderItem.id = :orderItemId")
    void  deleteByOrderItemId(Long orderItemId);
    @Transactional
    @Modifying
    void  deleteByShipmentId(Long shipmentId);

}
