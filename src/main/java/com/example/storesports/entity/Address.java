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

    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String zipCode;


    @OneToMany(mappedBy = "address",cascade = CascadeType.REMOVE)
    private List<UserAddressMapping> userAddressMappings;

}
