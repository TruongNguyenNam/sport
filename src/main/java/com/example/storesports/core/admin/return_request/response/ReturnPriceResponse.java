package com.example.storesports.core.admin.return_request.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnPriceResponse {
    private String code;
    private String userName;
    private String productName;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankName;
    private Double totalPrice;
    private Long idReturnRequestItem;
}
