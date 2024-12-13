package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "UserSupportResponse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSupportResponse extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "ticket_id")
        private UserSupportTicket ticket;

        private String response;
        private java.util.Date respondedAt;



}
