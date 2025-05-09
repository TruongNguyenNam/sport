package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "order_id")
        private Order order;

        private Double amount;
        private LocalDateTime paymentDate;

        @ManyToOne
        @JoinColumn(name = "payment_method_id", nullable = false)
        private PaymentMethod paymentMethod;

        @Enumerated(EnumType.STRING)
        private PaymentStatus paymentStatus;



}
