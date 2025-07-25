package com.example.storesports.core.auth.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateUserForm {
    private String email;
    private String phoneNumber;
    private String gender;
    private UserAddress address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAddress {
        private Long id;
        private String addressStreet;
        private String addressWard;
        private String addressCity;
        private String addressState;
        private String addressCountry;
        private String addressZipcode;
        private String addressDistrict;
        private String addressProvince;

    }

}
