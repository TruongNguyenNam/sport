package com.example.storesports.entity;

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

        private String code;

        private Double discountAmount;

        private Boolean status; // trạng thái

        private Integer quantity; //số lượng > 0

        private LocalDateTime expirationDate;
        private LocalDateTime startDate;
        private Boolean deleted; // 0 là chưa xoá, còn hạn  // 1

        @OneToMany(mappedBy = "coupon", cascade = CascadeType.REMOVE)
        private List<CouponUsage> couponUsages;



}
