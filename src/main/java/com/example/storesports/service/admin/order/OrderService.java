package com.example.storesports.service.admin.order;

import com.example.storesports.core.admin.history.payload.OrderHistoryResponse;
import com.example.storesports.core.admin.order.payload.*;
import com.example.storesports.entity.Order;
import com.example.storesports.infrastructure.constant.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

     OrderResponse findById(Long id);
     OrderResponse createInvoice(CreateInvoiceRequest request);
     OrderResponse addProductToOrder(OrderRequest request);

     OrderResponse addProductToOrderV3(OrderRequest request, HttpServletRequest httpServletRequest);

     List<OrderResponse> getAll();

     //Doanh Thu
     List<DailyRevenueResponse> getDailyRevenue();
     List<MonthlyRevenueResponse> getMonthlyRevenue();
     List<YearlyRevenueResponse> getYearlyRevenue();
     //Đơn Hàng hủy
     OrderStatusTodayResponse getCancelledOrdersToday();
     OrderStatusMonthResponse getCancelledOrdersThisMonth();
     OrderStatusYearResponse getCancelledOrdersThisYear();

     //Đơn Hàng đã hoàn thành
     OrderStatusTodayResponse countCompletedOrdersToday();
     OrderStatusMonthResponse countCompletedOrdersThisMonth();
     OrderStatusYearResponse countCompletedOrdersThisYear();

     //Đơn Hàng hoàn
     OrderStatusTodayResponse getReturnedOrdersToday();
     OrderStatusMonthResponse getReturnedOrdersThisMonth();
     OrderStatusYearResponse getReturnedOrdersThisYear();

     //test
     CustomStatisticalResponse getStatisticsBetween(LocalDate startDate, LocalDate endDate);

     List<MonthlyOrderTypeResponse> getMonthlyOrderChart();

     //test lại để làm xác nhận đơn hàng
     // test khi thêm và thanh toán sản phẩm
     OrderResponse addProductToOrderV2(OrderRequest request);


     OrderResponse updateOrderStatus(String orderCode, OrderStatus newStatus, String nodes);

//     Order findOrderByCode(String orderCode);
     OrderResponse editOrderItems(String code, OrderRequest request); // cái này đang sai

     List<OrderResponse> getAllByShip();

     List<OrderResponse> getAllOrderStatus(OrderStatus orderStatus);

      OrderResponse updateOrder(UpdateOrderRequest request);

      // thay cái này
      List<OrderStatusCount> getOrderStatusCounts();

      OrderResponse addItemToOrder(String orderCode, OrderRequest.OrderItemRequest itemRequest);

      OrderResponse removeItemFromOrder(String orderCode, Long productId, Integer quantity);

      void deleteOrder(String orderCode);

     List<OrderResponse> getPendingOrders();

      List<OrderHistoryResponse> getOrderHistory(Long orderId);


}
