package com.example.storesports.repositories;


import com.example.storesports.entity.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem,Long> {




}
