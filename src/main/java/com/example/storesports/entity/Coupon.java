package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "Coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String code;
        private Double discountAmount;
        private java.util.Date expirationDate;

        @OneToMany(mappedBy = "coupon", cascade = CascadeType.REMOVE)
        private List<CouponUsage> couponUsages;



}
