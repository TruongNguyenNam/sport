package com.example.storesports.service.admin.history;

import com.example.storesports.core.admin.history.payload.OrderHistoryResponse;
import com.example.storesports.entity.Order;
import com.example.storesports.entity.OrderHistory;
import com.example.storesports.infrastructure.constant.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public interface OrderHistoryService {
//    List<OrderHistoryResponse> getHistory(String )

// List<OrderHistory> getOrderHistory(String orderCode) {
//        Order order = orderRepository.findByOrderCode(orderCode)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        List<OrderHistory> histories = new ArrayList<>();
//
//        // Các trạng thái theo thứ tự
//        List<OrderStatus> statuses = List.of(
//                OrderStatus.PENDING,
//                OrderStatus.CONFIRMED,
//                OrderStatus.SHIPPED,
//                OrderStatus.COMPLETED,
//                OrderStatus.CANCELLED,
//                OrderStatus.RETURNED
//        );
//
//        // Lấy index của trạng thái hiện tại
//        int currentIndex = statuses.indexOf(order.getOrderStatus());
//
//        // Nếu trạng thái không hợp lệ thì bỏ qua
//        if (currentIndex == -1) {
//            return histories;
//        }
//
//        // Build lịch sử cho từng bước đã đi qua
//        for (int i = 0; i <= currentIndex; i++) {
//            OrderStatus status = statuses.get(i);
//
//            OrderHistory history = new OrderHistory();
//            history.setCreatedDate(order.getCreatedDate()); // bạn có thể thay bằng thời gian thay đổi thật sự
//            history.setLastModifiedDate(order.getLastModifiedDate());
//            history.setOrderStatus(status.name());
//            history.setNodes(order.getNodes());
//
//            histories.add(history);
//        }
//
//        return histories;
//    }

}
