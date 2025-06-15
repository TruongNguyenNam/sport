package com.example.storesports.service.admin.orderItem.impl;


import com.example.storesports.core.admin.orderItem.payload.*;
import com.example.storesports.repositories.OrderItemRepository;
import com.example.storesports.service.admin.orderItem.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;

    @Override
    public SoldQuantityResponse getTotalSoldQuantityByDay() {
        LocalDate today = LocalDate.now();
        Long quantity = orderItemRepository.getTotalSoldQuantityByDate(today);
        return new SoldQuantityResponse(quantity != null ? quantity : 0L, today);
    }

    @Override
    public SoldQuantityByMonthResponse getTotalSoldQuantityThisMonth() {
        Long quantity = orderItemRepository.getTotalSoldQuantityByMonth();
        YearMonth currentMonth = YearMonth.now(); // "2025-06"
        return new SoldQuantityByMonthResponse(quantity == null ? 0 : quantity, currentMonth.toString());
    }

    @Override
    public SoldQuantityByYearResponse getTotalSoldQuantityThisYear() {
        Long quantity = orderItemRepository.getTotalSoldQuantityByYear();
        int year = LocalDate.now().getYear(); // 2025
        return new SoldQuantityByYearResponse(quantity == null ? 0 : quantity, year);
    }
    // San phẩm bán chạy nhất trong ngay
    @Override
    public List<SellingProductsResponse> getTop30SellingProductsToday() {
        return orderItemRepository.findTop30SellingProductsToday()
                .stream()
                .map(p -> new SellingProductsResponse(
                        p.getId(),
                        p.getImgUrl(),
                        p.getProductName(),
                        p.getSoldQuantity(),
                        p.getPercentage(),
                        p.getOrderDate()
                ))
                .toList();
    }
    @Override
    public List<SellingProductsResponse> getTop30SellingProductsThisMonth() {
        return orderItemRepository.findTop30SellingProductsThisMonth()
                .stream()
                .map(p -> new SellingProductsResponse(
                        p.getId(),
                        p.getImgUrl(),
                        p.getProductName(),
                        p.getSoldQuantity(),
                        p.getPercentage(),
                        p.getOrderDate()
                ))
                .toList();
    }
    @Override
    public List<SellingProductsResponse> getTop30SellingProductsThisYear() {
        return orderItemRepository.findTop30SellingProductsThisYear()
                .stream()
                .map(p -> new SellingProductsResponse(
                        p.getId(),
                        p.getImgUrl(),
                        p.getProductName(),
                        p.getSoldQuantity(),
                        p.getPercentage(),
                        p.getOrderDate()
                ))
                .toList();
    }
    @Override
    public List<SellingProductsResponse> getTopSellingProductsBetween(LocalDate startDate, LocalDate endDate) {
        return orderItemRepository.findTopSellingProductsBetween(startDate, endDate)
                .stream()
                .map(p -> new SellingProductsResponse(
                        p.getId(),
                        p.getImgUrl(),
                        p.getProductName(),
                        p.getSoldQuantity(),
                        p.getPercentage(),
                        p.getOrderDate()
                ))
                .toList();
    }
    @Override
    public Long getTotalSoldQuantityBetween(LocalDateTime startDate, LocalDateTime endDate) {
        Long result = orderItemRepository.getTotalSoldQuantityBetween(startDate, endDate);
        return result != null ? result : 0L;
    }


}

