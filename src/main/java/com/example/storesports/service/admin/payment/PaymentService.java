package com.example.storesports.service.admin.payment;

import com.example.storesports.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment createPayment(Payment payment);
    Payment updatePayment(Payment payment);
    Optional<Payment> getPaymentById(Long id);
    Optional<Payment> getPaymentByOrderId(Long orderId);
    List<Payment> getAllPayments();
    void deletePayment(Long id);
}
