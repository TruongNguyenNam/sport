package com.example.storesports.service.client.payment_method.impl;

import com.example.storesports.core.admin.payment_method.payload.PaymentMethodResponse;
import com.example.storesports.core.client.payment_method.payload.PaymentMethodClientResponse;
import com.example.storesports.entity.PaymentMethod;
import com.example.storesports.repositories.PaymentMethodRepository;
import com.example.storesports.service.client.payment_method.PaymentMethodClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodClientServiceImpl implements PaymentMethodClientService {
    private final PaymentMethodRepository paymentMethodRepository;

    private final ModelMapper modelMapper;
    @Override
    public List<PaymentMethodClientResponse> getAllPaymentMethod() {
        List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        if(paymentMethods.isEmpty()){
            throw new IllegalArgumentException("danh sách phương thức thanh toán bị trống"+paymentMethods);
        }

        return paymentMethods.stream()
                .map(paymentMethod -> modelMapper.map(paymentMethod, PaymentMethodClientResponse.class))
                .collect(Collectors.toList());

    }
}
