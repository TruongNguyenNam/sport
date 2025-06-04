package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.DiscountStatus;
import jakarta.persistence.*;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;




import java.time.LocalDateTime;


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


       private Double priceThreshold;

        private LocalDateTime startDate;

        private LocalDateTime endDate;



        @Enumerated(EnumType.STRING)
        private DiscountStatus status;


        private Boolean deleted; // 0 là chưa xoá, còn hạn  // 1






}
