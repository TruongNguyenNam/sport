package com.example.storesports.repositories;


import com.example.storesports.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {


    boolean existsBySku(String sku);

    List<Product> findByParentProductId(Long id);

    Optional<Product> findByParentProductIdAndSku(Long parentProductId, String sku);
    void deleteByParentProductId(Long parentProductId);

    @Query("select p from Product p where p.parentProductId is null order by  p.id desc")
    List<Product> findAllParentProducts();


}
