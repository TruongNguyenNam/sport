package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.MediaStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "return_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnMedia {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private  Long id;
    @ManyToOne
    @JoinColumn(name = "return_request_item_id")
    private ReturnRequestItem returnRequestItems;

    private String url;

    @Enumerated(EnumType.STRING)
    private MediaStatus type;
}
