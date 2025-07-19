package com.example.storesports.core.admin.address.payload;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AddressResponse {
    private Long id;
    private String street;
    private String ward; // Phường (Phường Phúc Đồng)
    private String city;
    private String state;
    private String country;
    private String zipcode;
    private String district; // Quận/Huyện (Huyện Vĩnh Tuy)
    private String province; // Tỉnh (Quận Long Biên)
    private String receiverName;
    private String receiverPhone;
    private Boolean isDefault;

}
