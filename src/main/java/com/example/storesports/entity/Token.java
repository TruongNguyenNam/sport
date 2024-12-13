package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
@Entity
@Table(name = "Token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NonNull
    private User user;

    @Column(name = "key", length = 100, nullable = false, unique = true)
    @NonNull
    private String key;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    @NonNull
    private Type type;

    @Column(name = "expired_date")
    @Temporal(TemporalType.DATE)
    @NonNull
    private Date expiredDate;



    public Token(@NonNull User user, @NonNull String key, @NonNull Type type, @NonNull Date expiredDate) {
        this.user = user;
        this.key = key;
        this.type = type;
        this.expiredDate = expiredDate;
    }

    @NoArgsConstructor
    @Getter
    public enum Type {
        REFRESH_TOKEN, REGISTER, FORGOT_PASSWORD
    }

}
