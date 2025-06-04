package com.example.storesports.core.admin.payment_method.payload;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class PaymentMethodRequest {

    private Long id;
    private String name;
    private String description;




}
