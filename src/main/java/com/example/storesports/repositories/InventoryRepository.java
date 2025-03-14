package com.example.storesports.repositories;


import com.example.storesports.entity.Inventory;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository  extends JpaRepository<Inventory,Long> {

    @Query("FROM Inventory i WHERE i.product.id = :proId")
    Optional<Inventory> findByProductId(@Param("proId") Long productId);


//    @Query("FROM Inventory i WHERE i.product.id = :proId")
//    List<Inventory> findAllByProductId(@Param("proId") Long productId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Inventory i WHERE i.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

}
