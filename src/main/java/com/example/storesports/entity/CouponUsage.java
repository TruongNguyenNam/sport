package com.example.storesports.entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "CouponUsage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsage extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "coupon_id")
        private Coupon coupon;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        private Date usedDate;

        private Boolean deleted;


}
