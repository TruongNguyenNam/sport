package com.example.storesports.entity;

import jakarta.persistence.*;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

        private java.util.Date startDate;

        private java.util.Date endDate;

//        @ManyToOne
//        @JoinColumn(name = "sport_type_id")
//        private SportType sportType;
        // Getters and Setters


}
