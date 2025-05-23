package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.CouponStatus;
import com.example.storesports.infrastructure.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

        private String code; //UUID

        private Double discountAmount;

        @Enumerated(EnumType.STRING)
        private CouponStatus couponStatus;

        private Integer quantity; //số lượng > 0

        private LocalDateTime startDate;

        private LocalDateTime expirationDate;

        private Boolean deleted; // 0 là chưa xoá, còn hạn  // 1

        @OneToMany(mappedBy = "coupon", cascade = CascadeType.REMOVE)
        private List<CouponUsage> couponUsages;



}
