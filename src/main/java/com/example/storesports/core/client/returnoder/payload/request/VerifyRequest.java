package com.example.storesports.core.client.returnoder.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyRequest {
    private String bankCode;        // mã BIN ngân hàng
    private String accountNumber;
}
