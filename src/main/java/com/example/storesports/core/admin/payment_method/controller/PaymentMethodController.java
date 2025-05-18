package com.example.storesports.core.admin.payment_method.controller;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.payment_method.payload.PaymentMethodResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.payment_method.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payment_method")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseData<List<PaymentMethodResponse>> getAllPaymentMethod(){
        List<PaymentMethodResponse> paymentMethodResponses = paymentMethodService.getAll();
        return ResponseData.<List<PaymentMethodResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách phương thức thanh toán thành công")
                .data(paymentMethodResponses)
                .build();
    }




}
