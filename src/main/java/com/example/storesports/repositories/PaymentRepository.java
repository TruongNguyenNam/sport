package com.example.storesports.repositories;


import com.example.storesports.entity.Payment;
import com.example.storesports.entity.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository  extends JpaRepository<Payment,Long> {

    @Query("select p from Payment p where p.order.id = :id")
    Optional<Payment> findByOrderId(@Param("id") Long id);


//    @Query("from ProductAttributeValue a where a.product.id = :id")
//    List<ProductAttributeValue> findByProductId(@Param("id") Long id);

}
