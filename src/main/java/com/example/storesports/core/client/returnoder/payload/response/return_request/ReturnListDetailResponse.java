package com.example.storesports.core.client.returnoder.payload.response.return_request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnListDetailResponse {
    private String productName;
    private String status;
    private int quantity;
    private String note;
    private String reason;
    private Map<String,String> attribute;
    private String imageUrl;
}
