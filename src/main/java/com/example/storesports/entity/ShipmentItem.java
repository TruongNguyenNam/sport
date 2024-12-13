package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ShipmentItem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentItem extends Auditable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "shipment_id")
        private Shipment shipment;

        @ManyToOne
        @JoinColumn(name = "order_item_id")
        private OrderItem orderItem;


}
