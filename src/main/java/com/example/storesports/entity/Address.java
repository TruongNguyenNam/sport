package com.example.storesports.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "Address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String street;
    private String ward; // Phường (Phường Phúc Đồng)
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String district; // Quận/Huyện (Huyện Vĩnh Tuy)
    private String province; // Tỉnh (Quận Long Biên)
    private Boolean deleted;

    @OneToMany(mappedBy = "address",cascade = CascadeType.REMOVE)
    private List<UserAddressMapping> userAddressMappings;

}
