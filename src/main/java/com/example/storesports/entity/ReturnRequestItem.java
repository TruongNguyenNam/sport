package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "return_request_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "return_request_id")
    private ReturnRequest returnRequest;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Enumerated(EnumType.STRING)
    private ReturnRequestItemStatus status;

    private Integer quantity;

    private String reason;

    private String note;

    private Boolean deleted;
}
