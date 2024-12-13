package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ProductSpecificationOption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecificationOption extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        @ManyToOne
        @JoinColumn(name = "specification_id")
        private ProductSpecification specification;

        private String value;



}
