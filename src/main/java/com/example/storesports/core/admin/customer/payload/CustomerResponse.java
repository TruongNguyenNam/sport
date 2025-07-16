package com.example.storesports.core.admin.customer.payload;

import com.example.storesports.core.admin.address.payload.AddressResponse;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomerResponse {
    private Long id;
    private String email;
    private String username;
    private String phoneNumber;
    private String role;
    private boolean isActive;
    private String gender;
    private List<AddressResponse> addresses;
}
