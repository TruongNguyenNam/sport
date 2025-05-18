package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Entity
@Table(name = "ProductAttribute")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductAttribute extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String description;

        @OneToMany(mappedBy = "attribute",cascade = CascadeType.REMOVE)
        private List<ProductAttributeValue> productAttributeValues;



}
