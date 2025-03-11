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

//    @EntityGraph(attributePaths = {
//            "productSportTypeMappings.sportType",
//            "productTagMappings.tag",
//            "productImages",
//            "productSpecificationOptions.specification",
//            "inventories"
//    })
//    @Nullable
//    Optional<Product> findById(Long id);


    List<Product> findByParentProductId(Long id);

    Optional<Product> findByParentProductIdAndSku(Long parentProductId, String sku);
    void deleteByParentProductId(Long parentProductId);

}
