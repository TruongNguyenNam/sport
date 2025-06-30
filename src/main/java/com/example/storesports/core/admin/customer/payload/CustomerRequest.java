package com.example.storesports.core.admin.customer.payload;

import com.example.storesports.core.admin.address.payload.AddressRequest;
import com.example.storesports.infrastructure.constant.Gender;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerRequest {
    private String username;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private AddressRequest address;

}
