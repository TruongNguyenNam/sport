package com.example.storesports.core.admin.payment.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class PaymentResponse {
    private Long id; // ID của thanh toán
    private Double amount; // Số tiền thanh toán
    private String paymentStatus; // Trạng thái thanh toán (COMPLETED, PENDING, v.v.)
    private LocalDateTime paymentDate; // Thời gian thanh toán
    private String paymentMethodName; // Tên phương thức thanh toán (lấy từ PaymentMethodMapping)

}
