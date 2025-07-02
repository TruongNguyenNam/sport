package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "PaymentMethod")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod extends Auditable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "paymentMethod", cascade = CascadeType.REMOVE)
    private List<Payment> payments;

    private Boolean deleted;
//    @OneToMany(mappedBy = "paymentMethod",cascade = CascadeType.REMOVE)
//    private List<PaymentMethodMapping> paymentMethodMappings;
}
