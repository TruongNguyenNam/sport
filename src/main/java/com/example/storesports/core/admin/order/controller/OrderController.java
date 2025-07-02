package com.example.storesports.core.admin.order.controller;

import com.example.storesports.core.admin.order.payload.*;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseData<OrderResponse> createOrder(@RequestBody CreateInvoiceRequest request) {
        try {
            OrderResponse response = orderService.createInvoice(request);
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Tạo đơn hàng thành công")
                    .data(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Yêu cầu không hợp lệ: " + e.getMessage())
                    .data(null)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.<OrderResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi server: " + e.getMessage())
                    .data(null)
                    .build();
        }
    }


    @GetMapping("/pos")
    public ResponseData<List<OrderResponse>> getAllPosOrders() {
        List<OrderResponse> orders = orderService.getAll();
        return ResponseData.<List<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đơn hàng POS thành công")
                .data(orders)
                .build();
    }

    @PostMapping("/{orderCode}/products")
    public ResponseData<OrderResponse> addProductToOrder(
            @PathVariable String orderCode,
            @RequestBody OrderRequest request) {
        request.setOrderCode(orderCode);
        OrderResponse response = orderService.addProductToOrder(request);
        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thêm sản phẩm vào đơn hàng thành công")
                .data(response)
                .build();
    }


    @GetMapping("/{id}")
    public ResponseData<OrderResponse> findById(@PathVariable(name = "id") Long id){
        OrderResponse response = orderService.findById(id);
        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("thông tin của đơn hàng")
                .data(response)
                .build();
    }


    @PostMapping("/{orderCode}/details")
    public ResponseEntity<OrderResponse> addOrderDetails(
            @PathVariable String orderCode,
            @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.addOrderDetails(orderCode, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chart/monthly-orders")
    public ResponseData<List<MonthlyOrderTypeResponse>> getMonthlyOrderChart() {
        List<MonthlyOrderTypeResponse> data = orderService.getMonthlyOrderChart();
        return ResponseData.<List<MonthlyOrderTypeResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng theo tháng (bán thường & ship)")
                .data(data)
                .build();
    }


    // Thống Ke doanh thu theo tháng với trạng thái đã hoàn thành và đã thanh toán
    @GetMapping("/revenue/daily")
    public ResponseData<List<DailyRevenueResponse>> getDailyRevenue() {
        List<DailyRevenueResponse> revenueList = orderService.getDailyRevenue();
        return ResponseData.<List<DailyRevenueResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu theo ngày hôm nay")
                .data(revenueList)
                .build();
    }
    @GetMapping("/revenue/monthly")
    public ResponseData<List<MonthlyRevenueResponse>> getMonthlyRevenue() {
        List<MonthlyRevenueResponse> revenueList = orderService.getMonthlyRevenue();
        return ResponseData.<List<MonthlyRevenueResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu theo tháng")
                .data(revenueList)
                .build();
    }

    @GetMapping("/revenue/yearly")
    public ResponseData<List<YearlyRevenueResponse>> getYearlyRevenue() {
        List<YearlyRevenueResponse> revenueList = orderService.getYearlyRevenue();
        return ResponseData.<List<YearlyRevenueResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu theo năm")
                .data(revenueList)
                .build();
    }
    // Thống kê số lượng đơn hàng đã hủy hôm nay
    @GetMapping("/cancelled/today")
    public ResponseData<OrderStatusTodayResponse> getCancelledToday() {
        return ResponseData.<OrderStatusTodayResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng huỷ hôm nay")
                .data(orderService.getCancelledOrdersToday())
                .build();
    }
    // Thống kê số lượng đơn hàng đã hủy trong tháng
    @GetMapping("/cancelled/month")
    public ResponseData<OrderStatusMonthResponse> getCancelledThisMonth() {
        return ResponseData.<OrderStatusMonthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng huỷ trong tháng")
                .data(orderService.getCancelledOrdersThisMonth())
                .build();
    }
    // Thống kê số lượng đơn hàng đã hủy trong năm
    @GetMapping("/cancelled/year")
    public ResponseData<OrderStatusYearResponse> getCancelledThisYear() {
        return ResponseData.<OrderStatusYearResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng huỷ trong năm")
                .data(orderService.getCancelledOrdersThisYear())
                .build();
    }
    // Thống kê số lượng đơn hàng đã hoàn thành hôm nay
    @GetMapping("/completed/today")
    public ResponseData<OrderStatusTodayResponse> getCompletedToday() {
        return ResponseData.<OrderStatusTodayResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng đã hoàn thành hôm nay")
                .data(orderService.countCompletedOrdersToday())
                .build();
    }
    // Thống kê số lượng đơn hàng đã hoàn thành trong tháng
    @GetMapping("/completed/month")
    public ResponseData<OrderStatusMonthResponse> getCompletedThisMonth() {
        return ResponseData.<OrderStatusMonthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng đã hoàn thành trong tháng")
                .data(orderService.countCompletedOrdersThisMonth())
                .build();
    }
    // Thống kê số lượng đơn hàng đã hoàn thành trong năm
    @GetMapping("/completed/year")
    public ResponseData<OrderStatusYearResponse> getCompletedThisYear() {
        return ResponseData.<OrderStatusYearResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng đã hoàn thành trong năm")
                .data(orderService.countCompletedOrdersThisYear())
                .build();
    }
    // Thống kê số lượng đơn hàng đã trả lại hôm nay
    @GetMapping("/returned/today")
    public ResponseData<OrderStatusTodayResponse> getReturnedToday() {
        return ResponseData.<OrderStatusTodayResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng đã trả lại hôm nay")
                .data(orderService.getReturnedOrdersToday())
                .build();
    }
    // Thống kê số lượng đơn hàng đã trả lại trong tháng
    @GetMapping("/returned/month")
    public ResponseData<OrderStatusMonthResponse> getReturnedThisMonth() {
        return ResponseData.<OrderStatusMonthResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng đã trả lại trong tháng")
                .data(orderService.getReturnedOrdersThisMonth())
                .build();
    }
    // Thống kê số lượng đơn hàng đã trả lại trong năm
    @GetMapping("/returned/year")
    public ResponseData<OrderStatusYearResponse> getReturnedThisYear() {
        return ResponseData.<OrderStatusYearResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê đơn hàng đã trả lại trong năm")
                .data(orderService.getReturnedOrdersThisYear())
                .build();
    }
    // Thống kê tuỳ chỉnh giữa hai ngày
    @GetMapping("/custom")
    public ResponseData<CustomStatisticalResponse> getStatisticsBetweenDates(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseData.<CustomStatisticalResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Thống kê doanh thu và đơn hàng trong khoảng thời gian")
                .data(orderService.getStatisticsBetween(fromDate, toDate))
                .build();
    }
    

}
