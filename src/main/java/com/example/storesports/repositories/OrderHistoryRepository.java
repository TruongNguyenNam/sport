package com.example.storesports.repositories;

import com.example.storesports.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory,Long> {
    List<OrderHistory> findByOrderIdOrderByCreatedDateAsc(Long orderId);
}
