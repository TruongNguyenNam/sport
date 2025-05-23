package com.example.storesports.repositories;


import com.example.storesports.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {

    boolean existsBySku(String sku);
    @Query("SELECT p FROM Product p WHERE p.parentProductId= :id and p.deleted = false order by p.id desc ")  // fix product
    List<Product> findByParentProductId(@Param("id") Long id);

    Optional<Product> findByParentProductIdAndSku(Long parentProductId, String sku);
    void deleteByParentProductId(Long parentProductId);

    @Query("select p from Product p where p.parentProductId is null order by  p.id desc")
    List<Product> findAllParentProducts();


    @Query("select p from Product p where p.parentProductId is not null and p.deleted = false order by p.id desc")
    List<Product> findAllChildProduct();

    @Query("SELECT p FROM Product p JOIN ProductDiscountMapping pdm ON p.id = pdm.product.id WHERE pdm.discount.id = :discountId")
    List<Product> findProductsByDiscountId(@Param("discountId") Long discountId);



}
