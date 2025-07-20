package com.example.storesports.repositories;


import com.example.storesports.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod,Long> {

     Optional<PaymentMethod> findByName(String name);

}
