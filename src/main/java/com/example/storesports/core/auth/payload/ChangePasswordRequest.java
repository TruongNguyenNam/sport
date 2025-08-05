package com.example.storesports.core.auth.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangePasswordRequest {
    private String currentPassword; // mật khẩu cũ
    private String newPassword;     // mật khẩu mới
    private String confirmPassword;

}
