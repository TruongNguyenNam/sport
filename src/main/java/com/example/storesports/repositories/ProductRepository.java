package com.example.storesports.repositories;


import com.example.storesports.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {
            "productSportTypeMappings.sportType",
            "productTagMappings.tag",
            "productImages",
            "productSpecificationOptions.specification",
            "inventories"
    })
    @Nullable
    Optional<Product> findById(Long id);


}
