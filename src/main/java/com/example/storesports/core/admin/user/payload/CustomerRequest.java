package com.example.storesports.core.admin.user.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerRequest {
    private String email;
    private String username;
    private String phoneNumber;

}
