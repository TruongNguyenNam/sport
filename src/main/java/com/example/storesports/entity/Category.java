package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Entity
@Table(name = "Category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String description;

        private Boolean deleted;

        @OneToMany(mappedBy = "category",cascade = CascadeType.REMOVE)
        private List<Product> products;



}
