package com.example.storesports.entity;

import com.example.storesports.Infrastructure.constant.SportTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "SportType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SportType extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @Enumerated(EnumType.STRING)
        private SportTypeEnum sportTypeEnum;
        private String description;

        @OneToMany(mappedBy = "sportType",cascade = CascadeType.REMOVE)
        private List<Product> products;

        @OneToMany(mappedBy = "sportType",cascade = CascadeType.REMOVE)
        private List<Discount> discounts;



}
