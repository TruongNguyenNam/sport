package com.example.storesports.repositories;


import com.example.storesports.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductTagRepository  extends JpaRepository<ProductTag,Long> {



}
