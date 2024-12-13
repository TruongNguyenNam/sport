package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Entity
@Table(name = "Order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        private Double orderTotal;
        private Date orderDate;

        @Enumerated(EnumType.STRING)
        private OrderStatus orderStatus;

        @OneToMany(mappedBy = "order",cascade = CascadeType.REMOVE)
        private List<OrderItem> orderItems;

        @OneToMany(mappedBy = "order", cascade = CascadeType.REMOVE)
        private List<Shipment> shipments;

        @OneToMany(mappedBy = "order",cascade = CascadeType.REMOVE)
        private List<ReturnRequest> returnRequests;

        @OneToMany(mappedBy = "order",cascade = CascadeType.REMOVE)
        private List<Payment> payments;

        @OneToOne(mappedBy = "order",cascade = CascadeType.REMOVE)
        private Invoice invoice;
        // Getters and Setters


}
