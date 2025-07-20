package com.example.storesports.infrastructure.scheduler;

import com.example.storesports.entity.Order;
import com.example.storesports.entity.Shipment;
import com.example.storesports.infrastructure.constant.OrderStatus;
import com.example.storesports.repositories.OrderRepository;
import com.example.storesports.repositories.ShipmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;

    private final ShipmentRepository shipmentRepository;
//    @Scheduled(fixedRate = 30000)
//    @Transactional
//    public void updateExpiredOrders() {
//        try {
//            System.out.println("Scheduler đang chạy lúc: " + LocalDateTime.now());
//
//            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
//            List<Order> pendingOrders = orderRepository.findByOrderStatusAndDeletedFalseAndCreatedDateBefore(OrderStatus.PENDING, oneMinuteAgo);
//
//            System.out.println(" Số đơn hàng sẽ huỷ: " + pendingOrders.size());
//
//            for (Order order : pendingOrders) {
//                order.setOrderStatus(OrderStatus.CANCELLED);
//            }
//            orderRepository.saveAll(pendingOrders);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//    @Scheduled(cron = "0 0 * * * ?")
//    @Transactional
//    public void updateOrderStatus() {
//        List<Order> shippedOrders = orderRepository.findByOrderStatusAndDeletedFalseAndOrderSource(OrderStatus.SHIPPED);
//
//        for (Order order : shippedOrders) {
//            Shipment shipment = shipmentRepository.findByOrderId(order.getId())
//                    .stream()
//                    .findFirst()
//                    .orElse(null);
//            if (shipment != null && shipment.getEstimatedDeliveryDate() != null
//                    && shipment.getEstimatedDeliveryDate().isBefore(LocalDateTime.now())) {
//                order.setOrderStatus(OrderStatus.COMPLETED);
//                order.setDeleted(true);
//                order.setLastModifiedDate(LocalDateTime.now());
//                orderRepository.save(order);
//                log.info("Cập nhật trạng thái đơn hàng {} thành COMPLETED", order.getOrderCode());
//            } else if (shipment == null) {
//                log.warn("Không tìm thấy shipment cho đơn hàng {}", order.getOrderCode());
//            }
//        }
//    }
}
