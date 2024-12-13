package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Invoice extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne
        @JoinColumn(name = "order_id",referencedColumnName = "id")
        private Order order;


        private String invoiceNumber;
        private java.util.Date invoiceDate;
        private Double totalAmount;



}
