package com.example.storesports.repositories;

import com.example.storesports.entity.Product;
import com.example.storesports.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist,Long>, JpaSpecificationExecutor<Wishlist> {

    @Query("select p from Wishlist p where p.user.id= :userId and p.deleted = false ")
    List<Wishlist> findByUserIdAndDeletedFalse(@Param("userId") Long userId);



}
