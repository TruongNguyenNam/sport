package com.example.storesports.repositories;


import com.example.storesports.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute,Long>, JpaSpecificationExecutor<ProductAttribute> {

    @Query("select p from  ProductAttribute p order by p.id asc ")
    List<ProductAttribute> findAllProductAttribute();

    @Query("select count (p) from ProductAttribute p where p.name=?1")
    int countProductAttribute(String name);

    @Query("SELECT COUNT(p) FROM ProductAttribute p WHERE p.name = :name AND p.id != :id")
    int countProductAttributeByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

}
