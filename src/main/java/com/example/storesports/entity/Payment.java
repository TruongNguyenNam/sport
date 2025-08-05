package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "Payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "order_id")
        private Order order;

        // đây là khách lúc đưa tiền
        // lúc này trên phía clinent sẽ hiểu được là đưa bao nhiêu
        private Double amount;

        // tiền thừa khi đưa khách hàng đưa thừa
        private Double changeAmount;

        @Column(name = "transaction_id")
        private String transactionId;

        private String returnUrl;

        private LocalDateTime paymentDate;

        private Boolean deleted;

        @ManyToOne
        @JoinColumn(name = "payment_method_id", nullable = false)
        private PaymentMethod paymentMethod;  //chọn kiểu thanh toán

        @Enumerated(EnumType.STRING)
        private PaymentStatus paymentStatus;



}
