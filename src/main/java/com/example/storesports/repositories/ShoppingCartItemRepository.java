package com.example.storesports.repositories;


import com.example.storesports.entity.Product;
import com.example.storesports.entity.ShoppingCartItem;
import com.example.storesports.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem,Long>, JpaSpecificationExecutor<ShoppingCartItem> {


   @Query("SELECT s FROM ShoppingCartItem s WHERE s.user.id = :userId AND s.product.id = :productId")
   Optional<ShoppingCartItem> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

   @Query("select s from ShoppingCartItem s where s.user.id = :userId and s.deleted = false")
   List<ShoppingCartItem> findByUserId(@Param("userId") Long userId);


   @Query("SELECT COUNT(s) FROM ShoppingCartItem s WHERE s.user.id = :userId AND s.deleted = false")
   long countByUserIdAndDeletedFalse(@Param("userId") Long userId);

}
