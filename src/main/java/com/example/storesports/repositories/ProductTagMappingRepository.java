package com.example.storesports.repositories;


import com.example.storesports.entity.ProductTag;
import com.example.storesports.entity.ProductTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTagMappingRepository  extends JpaRepository<ProductTagMapping,Long> {


}
