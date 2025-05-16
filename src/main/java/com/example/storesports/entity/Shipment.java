package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Entity
@Table(name = "Shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "order_id")
        private Order order;

        private String trackingNumber;
        private java.util.Date shipmentDate;

        @Enumerated(EnumType.STRING)
        private ShipmentStatus shipmentStatus;

        private String carrier;

        private LocalDateTime estimatedDeliveryDate;

        @OneToMany(mappedBy = "shipment",cascade = CascadeType.REMOVE)
        private List<ShipmentItem> shipmentItems;

}
