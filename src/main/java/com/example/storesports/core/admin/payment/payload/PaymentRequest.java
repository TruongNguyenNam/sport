package com.example.storesports.core.admin.payment.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private Long paymentMethodId; // Phương thức thanh toán (CASH, CARD, MOBILE)
    private double amount;

}
