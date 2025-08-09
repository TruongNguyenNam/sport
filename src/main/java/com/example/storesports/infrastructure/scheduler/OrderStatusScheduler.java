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
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelPendingOrdersWithoutItems() {
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(1);
            List<Order> ordersToCancel = orderRepository.findEmptyPendingOrdersBefore(OrderStatus.PENDING, fiveMinutesAgo);

            if (!ordersToCancel.isEmpty()) {
                log.info("Tìm thấy {} đơn hàng PENDING không có sản phẩm, sẽ huỷ...", ordersToCancel.size());

                for (Order order : ordersToCancel) {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    order.setLastModifiedDate(LocalDateTime.now());
                    order.setOrderTotal(0.0);
                }

                orderRepository.saveAll(ordersToCancel);
                log.info("Đã huỷ {} đơn hàng không có sản phẩm", ordersToCancel.size());
            } else {
                log.info("Không có đơn nào cần huỷ.");
            }

        } catch (Exception e) {
            log.error("Lỗi khi chạy scheduler huỷ đơn hàng:", e);
        }
    }


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
