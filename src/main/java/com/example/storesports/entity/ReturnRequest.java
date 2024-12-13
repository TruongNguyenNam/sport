package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ReturnRequest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "order_id")
        private Order order;

        private Integer reasonId;
        private java.util.Date requestDate;


        // Getters and Setters


}
