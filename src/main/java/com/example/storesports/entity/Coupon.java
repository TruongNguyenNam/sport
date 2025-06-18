package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.CouponStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "coupon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String couponName;

        @Column( updatable = false, nullable = false, unique = true, length = 8)
        private String codeCoupon;

        private Double discountAmount;

        @Enumerated(EnumType.STRING)
        private CouponStatus couponStatus;

        private LocalDateTime startDate;

        private LocalDateTime expirationDate;

        private Boolean deleted;

        @OneToMany(mappedBy = "coupon", cascade = CascadeType.REMOVE)
        private List<CouponUsage> couponUsages;

        public Coupon(Long id) {
                this.id = id;
        }
}