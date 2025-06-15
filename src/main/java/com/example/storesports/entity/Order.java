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

        private String orderCode;

        private Double orderTotal; // tổng số tiền

        private Date orderDate;

        private Boolean isPos;  // true là bán thường // false là bán ship

        private String nodes;

        private Boolean deleted;  // xoá mềm

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


}
