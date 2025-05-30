package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ProductDiscountMapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscountMapping extends Auditable {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        @ManyToOne
        @JoinColumn(name = "discount_id")
        private Discount discount;

        private Boolean deleted;

}
