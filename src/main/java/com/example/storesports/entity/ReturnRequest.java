package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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

        @ManyToOne
        @JoinColumn(name = "reason_id")
        private ReturnRequestReason returnRequestReason;

        private Date requestDate;

        private Boolean deleted;


}
