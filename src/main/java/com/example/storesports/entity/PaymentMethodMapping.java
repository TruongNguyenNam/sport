package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PaymentMethodMapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodMapping extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
