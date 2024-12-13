package com.example.storesports.core.auth.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class loginForm {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
