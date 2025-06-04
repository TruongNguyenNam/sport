package com.example.storesports.core.admin.order.controller;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.order.payload.CreateInvoiceRequest;
import com.example.storesports.core.admin.order.payload.OrderRequest;
import com.example.storesports.core.admin.order.payload.OrderResponse;
import com.example.storesports.entity.Order;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseData<OrderResponse> findById(@PathVariable(name = "id") Long id){
        OrderResponse response = orderService.findById(id);
        return ResponseData.<OrderResponse>builder()
                .status(HttpStatus.OK.value())
                .message("thông tin của đơn hàng")
                .data(response)
                .build();
    }
//    @GetMapping("/{id}")
//    public ResponseData<CategoryResponse> getCategoryById(@PathVariable Long id) {
//        CategoryResponse categoryResponse = categoryService.findById(id);
//        return ResponseData.<CategoryResponse>builder()
//                .status(HttpStatus.OK.value())
//                .message("Lấy thông tin danh mục thành công")
//                .data(categoryResponse)
//                .build();
//    }


    @GetMapping("/pos")
    public ResponseData<List<OrderResponse>> getAllPosOrders() {
        List<OrderResponse> orders = orderService.getAll();
        return ResponseData.<List<OrderResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách đơn hàng POS thành công")
                .data(orders)
                .build();
    }

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



    @PostMapping("/{orderCode}/details")
    public ResponseEntity<OrderResponse> addOrderDetails(
            @PathVariable String orderCode,
            @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.addOrderDetails(orderCode, request);
        return ResponseEntity.ok(response);
    }





}
