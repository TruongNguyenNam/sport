package com.example.storesports.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "InventoryAdjustment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustment extends Auditable {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "product_id")
        private Product product;

        private Integer adjustmentQuantity;
        private String reason;
        private java.util.Date adjustmentDate;


}
