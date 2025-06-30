package com.example.storesports.service.client.payment_method;

import com.example.storesports.core.client.payment_method.payload.PaymentMethodClientResponse;

import java.util.List;

public interface PaymentMethodClientService {

    List<PaymentMethodClientResponse> getAllPaymentMethod();


}
