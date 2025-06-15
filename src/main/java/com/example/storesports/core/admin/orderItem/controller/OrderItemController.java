package com.example.storesports.core.admin.orderItem.controller;


import com.example.storesports.core.admin.orderItem.payload.*;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.orderItem.impl.OrderItemServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/orderItems")
@RequiredArgsConstructor
public class OrderItemController {
    @Autowired
    private OrderItemServiceImpl orderItemService;

    @GetMapping("/total-sold-quantity/day")
    public ResponseData<SoldQuantityResponse> getTotalSoldQuantityByDay() {
        SoldQuantityResponse response = orderItemService.getTotalSoldQuantityByDay();
        return ResponseData.<SoldQuantityResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tổng số lượng sản phẩm đã bán trong ngày hôm nay")
                .data(response)
                .build();
    }

    @GetMapping("/total-sold-quantity/month")
    public ResponseData<SoldQuantityByMonthResponse> getTotalSoldThisMonth() {
        return ResponseData.<SoldQuantityByMonthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tổng số lượng sản phẩm đã bán trong tháng hiện tại")
                .data(orderItemService.getTotalSoldQuantityThisMonth())
                .build();
    }

    @GetMapping("/total-sold-quantity/year")
    public ResponseData<SoldQuantityByYearResponse> getTotalSoldThisYear() {
        return ResponseData.<SoldQuantityByYearResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Tổng số lượng sản phẩm đã bán trong năm hiện tại")
                .data(orderItemService.getTotalSoldQuantityThisYear())
                .build();
    }

    // Lấy danh sách sản phẩm bán chạy nhất trong ngày
    @GetMapping("/top-selling-products-today")
    public ResponseData<List<SellingProductsResponse>> getTop30SellingProductsToday() {
        return ResponseData.<List<SellingProductsResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Top 30 sản phẩm bán chạy hôm nay")
                .data(orderItemService.getTop30SellingProductsToday())
                .build();
    }
    // Lấy danh sách sản phẩm bán chạy nhất trong tháng
    @GetMapping("/top-selling-products-month")
    public ResponseData<List<SellingProductsResponse>> getTop30SellingProductsThisMonth() {
        return ResponseData.<List<SellingProductsResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Top 30 sản phẩm bán chạy trong tháng này")
                .data(orderItemService.getTop30SellingProductsThisMonth())
                .build();
    }
    // Lấy danh sách sản phẩm bán chạy nhất trong năm
    @GetMapping("/top-selling-products-year")
    public ResponseData<List<SellingProductsResponse>> getTop30SellingProductsThisYear() {
        return ResponseData.<List<SellingProductsResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Top 30 sản phẩm bán chạy trong năm này")
                .data(orderItemService.getTop30SellingProductsThisYear())
                .build();
    }
    @GetMapping("/top-selling-products-between")
    public ResponseData<List<SellingProductsResponse>> getTopSellingProductsBetween(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseData.<List<SellingProductsResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Top 30 sản phẩm bán chạy từ " + startDate + " đến " + endDate)
                .data(orderItemService.getTopSellingProductsBetween(startDate, endDate))
                .build();
    }





}
