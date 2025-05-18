package com.example.storesports.core.admin.customer.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private String email;
    private String username;
    private String phoneNumber;
    private String role;
    private String addressStreet; // ok
    private String addressWard; // Phường (Phường Phúc Đồng)
    private String addressCity;  // ok
    private String addressState; // ok
    private String addressCountry;
    private String AddressZipcode;
    private String addressDistrict; // Quận/Huyện (Huyện Vĩnh Tuy)
    private String addressProvince; // Tỉnh (Quận Long Biên)
    private boolean isActive;

}
