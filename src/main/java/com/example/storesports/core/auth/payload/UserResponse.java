package com.example.storesports.core.auth.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class  UserResponse {
    private Long userId;
    private String username;
    private String email;
    private String message;
    private String role;
    private String password;
    private String phoneNumber;
    private String gender;
    private boolean isActive;

    private List<UserAddress> addresses;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAddress {
        private Long id;
        private String receiverName;
        private String receiverPhone;
        private String addressStreet; // ok
        private String addressWard; // Phường (Phường Phúc Đồng)
        private String addressCity;  // ok
        private String addressState; // ok
        private String addressCountry;
        private String AddressZipcode;
        private String addressDistrict; // Quận/Huyện (Huyện Vĩnh Tuy)
        private String addressProvince; // Tỉnh (Quận Long Biên)
        private Boolean isDefault;

    }
}
