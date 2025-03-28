package com.example.storesports.repositories;


import com.example.storesports.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductTagRepository  extends JpaRepository<ProductTag,Long> {

    @Query("select p from ProductTag p order by p.id desc ")
    List<ProductTag> findAllTags();


}
