package com.example.storesports.core.client.payment_method.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentMethodClientResponse {
    private Long id;

    private String name;
    private String description;

}
