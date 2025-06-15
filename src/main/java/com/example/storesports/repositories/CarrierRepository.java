package com.example.storesports.repositories;

import com.example.storesports.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarrierRepository extends JpaRepository<Carrier,Long>,JpaSpecificationExecutor<Carrier> {
}
