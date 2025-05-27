package com.example.storesports.entity;

import com.example.storesports.infrastructure.constant.SupportTicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "UserSupportTicket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSupportTicket extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        private String subject;
        private String description;

        @Enumerated(EnumType.STRING)
        private SupportTicketStatus supportTicketStatus;
        private java.util.Date createdAt;

        @OneToMany(mappedBy = "ticket",cascade = CascadeType.REMOVE)
        private List<UserSupportResponse> responses;

        private Boolean deleted;
        // Getters and Setters


}
