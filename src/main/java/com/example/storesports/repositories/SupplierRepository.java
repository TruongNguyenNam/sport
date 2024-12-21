package com.example.storesports.repositories;


import com.example.storesports.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SupplierRepository extends JpaRepository<Supplier,Long>, JpaSpecificationExecutor<Supplier> {




}
