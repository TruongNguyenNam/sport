package com.example.storesports.entity;

import com.example.storesports.Infrastructure.constant.Gender;
import com.example.storesports.Infrastructure.constant.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "User")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

//    @Enumerated(EnumType.STRING)
//    @Column(length = 20)
//    private Gender gender;


    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<UserAddressMapping> userAddressMappings;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<ProductReview> productReviews;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<ShoppingCartItem> shoppingCartItems;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<Order> orders;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<Wishlist> wishlists;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<UserSupportTicket> supportTickets;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<CouponUsage> couponUsages;

    @OneToMany(mappedBy = "user",cascade = CascadeType.REMOVE)
    private List<Token> tokens;

}
