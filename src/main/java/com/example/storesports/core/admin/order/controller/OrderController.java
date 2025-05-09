package com.example.storesports.core.admin.order.controller;

import com.example.storesports.core.admin.order.payload.CreateInvoiceRequest;
import com.example.storesports.core.admin.order.payload.OrderRequest;
import com.example.storesports.core.admin.order.payload.OrderResponse;
import com.example.storesports.service.admin.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateInvoiceRequest request) {
        try {
            OrderResponse response = orderService.createInvoice(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            System.err.println("Bad Request: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(); // log lỗi cụ thể
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{orderCode}/details")
    public ResponseEntity<OrderResponse> addOrderDetails(
            @PathVariable String orderCode,
            @RequestBody @Valid OrderRequest request
    ) {
        OrderResponse response = orderService.addOrderDetails(orderCode, request);
        return ResponseEntity.ok(response);
    }


}
