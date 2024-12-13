package com.example.storesports.entity;

import com.example.storesports.Infrastructure.constant.PaymentStatus;
import com.example.storesports.Infrastructure.constant.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        private java.util.Date paymentDate;
        @Enumerated(EnumType.STRING)
        private PaymentStatus paymentStatus;



}
