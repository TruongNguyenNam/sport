package com.example.storesports.service.admin.order;

import com.example.storesports.core.admin.order.payload.*;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

     OrderResponse findById(Long id);
     OrderResponse createInvoice(CreateInvoiceRequest request);
     OrderResponse addProductToOrder(OrderRequest request);
     OrderResponse addOrderDetails(String orderCode, OrderRequest request);

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

}
