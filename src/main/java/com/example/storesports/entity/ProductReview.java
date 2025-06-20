package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ProductReview")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        private Integer rating;
        private String reviewText;

        private Boolean deleted;

}
