package com.example.storesports.service.admin.orderItem;

import com.example.storesports.core.admin.orderItem.payload.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemService {
//    List<Object[]> getOrderItemByOrderCode(String orderCode);

    SoldQuantityResponse getTotalSoldQuantityByDay();
    SoldQuantityByMonthResponse getTotalSoldQuantityThisMonth();
    SoldQuantityByYearResponse getTotalSoldQuantityThisYear();

    List<SellingProductsResponse> getTop30SellingProductsToday();
    List<SellingProductsResponse> getTop30SellingProductsThisMonth();
    List<SellingProductsResponse> getTop30SellingProductsThisYear();
    List<SellingProductsResponse> getTopSellingProductsBetween(LocalDate startDate, LocalDate endDate);
    Long getTotalSoldQuantityBetween(LocalDateTime startDate, LocalDateTime endDate);



}
