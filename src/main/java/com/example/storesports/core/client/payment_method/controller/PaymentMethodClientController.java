package com.example.storesports.core.client.payment_method.controller;

import com.example.storesports.core.admin.payment_method.payload.PaymentMethodResponse;
import com.example.storesports.core.client.payment_method.payload.PaymentMethodClientResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.payment_method.PaymentMethodClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/client/payment_method")
@Validated
@RequiredArgsConstructor
@Tag(name = "PaymentMethod", description = "Endpoints for managing PaymentMethod")
@Slf4j
public class PaymentMethodClientController {
    private final PaymentMethodClientService paymentMethodClientService;

    @GetMapping
    public ResponseData<List<PaymentMethodClientResponse>> getAllPaymentMethod(){
        List<PaymentMethodClientResponse> paymentMethodResponses = paymentMethodClientService.getAllPaymentMethod();
        return ResponseData.<List<PaymentMethodClientResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách phương thức thanh toán thành công")
                .data(paymentMethodResponses)
                .build();
    }
}
