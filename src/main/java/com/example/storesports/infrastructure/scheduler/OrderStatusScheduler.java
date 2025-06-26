package com.example.storesports.infrastructure.scheduler;

import com.example.storesports.entity.Order;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateExpiredOrders() {
        try {
            System.out.println("Scheduler đang chạy lúc: " + LocalDateTime.now());

            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            List<Order> pendingOrders = orderRepository.findByOrderStatusAndDeletedFalseAndCreatedDateBefore(OrderStatus.PENDING, oneMinuteAgo);

            System.out.println("📦 Số đơn hàng sẽ huỷ: " + pendingOrders.size());

            for (Order order : pendingOrders) {
                order.setOrderStatus(OrderStatus.CANCELLED);
//                order.setDeleted(true);
            }
            orderRepository.saveAll(pendingOrders);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
