package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "ReturnRequestReason")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestReason extends Auditable {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;
        private String description;
        @OneToMany(mappedBy = "reason", cascade = CascadeType.REMOVE)
        private List<ReturnRequestItem> returnRequestItems;

        private Boolean deleted;
}
