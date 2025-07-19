package com.example.storesports.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "UserAddressMapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressMapping extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @JoinColumn(name = "address_id")
        private Address address;

        private String receiverName;   // Tên người nhận hàng

        private String receiverPhone;  // Số điện thoại nhận hàng

        private Boolean isDefault;     // Là địa chỉ mặc định không

        private Boolean deleted;
        // Getters and Setters

}
