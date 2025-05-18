package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.DiscountStatus;
import jakarta.persistence.*;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

<<<<<<< HEAD
=======

import java.time.LocalDateTime;

>>>>>>> e7231ef37979ba6d569c27bd5ff42f00fcbd66c0
@Entity
@Table(name = "Discount")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Discount extends Auditable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private Double discountPercentage;

<<<<<<< HEAD
        private Date startDate;

        private Date endDate;
=======
        private Double priceThreshold;

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        @Enumerated(EnumType.STRING)
        private DiscountStatus status;
>>>>>>> e7231ef37979ba6d569c27bd5ff42f00fcbd66c0



}
