package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.ReturnRequestItemStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

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
private String urlImageOderItem;
    @ManyToOne
    @JoinColumn(name = "return_request_id")
    private ReturnRequest returnRequest;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;



    @Enumerated(EnumType.STRING)
    private ReturnRequestItemStatus status;

    private Integer quantity;

    private String imageUrl;
    private String videoUrl;

    private String reason;

    private String note;

    private String adminNote; // Ghi chú phản hồi từ admin (lý do từ chối, v.v.)

    private Date respondedAt; // Thời gian phản hồi

    // optional:
    private String respondedBy; //ai phản hồi

    private Boolean deleted;

    @OneToMany(mappedBy = "returnRequestItems", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReturnMedia> returnMedias;

}
