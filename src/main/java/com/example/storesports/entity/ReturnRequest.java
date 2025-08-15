package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

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

        @Column(name = "code", unique = true, nullable = false)
        private String code;

        @ManyToOne
        @JoinColumn(name = "order_id")
        private Order order;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @CreationTimestamp
        private Date requestDate;

        private String note; // Ghi chú chung (nếu cần)

        @Column(name = "bank_account_number")
        private String bankAccountNumber;
        @Column(name = "bank_account_name")
        private String bankAccountName;
        @Column(name = "bank_name")
        private String bankName;
        private Boolean deleted;

        @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ReturnRequestItem> items;

}