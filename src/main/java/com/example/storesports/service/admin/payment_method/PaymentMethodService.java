package com.example.storesports.service.admin.payment_method;


import com.example.storesports.core.admin.payment_method.payload.PaymentMethodResponse;

import java.util.List;

public interface PaymentMethodService {

    List<PaymentMethodResponse> getAll();

}
