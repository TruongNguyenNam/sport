package com.example.storesports.service.admin.payment_method.impl;

import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.core.admin.payment_method.payload.PaymentMethodResponse;
import com.example.storesports.entity.PaymentMethod;
import com.example.storesports.repositories.PaymentMethodRepository;
import com.example.storesports.service.admin.payment_method.PaymentMethodService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {
    private final PaymentMethodRepository paymentMethodRepository;

    private final ModelMapper modelMapper;



    @Override
    public List<PaymentMethodResponse> getAll() {
      List<PaymentMethod> paymentMethods = paymentMethodRepository.findAll();
        if(paymentMethods.isEmpty()){
            throw new IllegalArgumentException("danh sách phương thức bị trống"+paymentMethods);
        }

        return paymentMethods.stream()
                .map(paymentMethod -> modelMapper.map(paymentMethod, PaymentMethodResponse.class))
                .collect(Collectors.toList());


    }



}
