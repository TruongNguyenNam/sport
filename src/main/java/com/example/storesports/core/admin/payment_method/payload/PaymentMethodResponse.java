package com.example.storesports.core.admin.payment_method.payload;

import com.example.storesports.entity.Payment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class PaymentMethodResponse {

    private Long id;

    private String name;
    private String description;

}
