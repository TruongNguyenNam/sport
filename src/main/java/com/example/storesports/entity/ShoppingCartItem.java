package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ShoppingCartItem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartItem extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        private Integer quantity;

        @Transient
        private Double totalPrice;

}
