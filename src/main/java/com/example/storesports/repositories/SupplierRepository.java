package com.example.storesports.repositories;


import com.example.storesports.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier,Long>, JpaSpecificationExecutor<Supplier> {

    @Query("select s from Supplier s order by s.id desc")
    List<Supplier> findAllSupplier();
}
