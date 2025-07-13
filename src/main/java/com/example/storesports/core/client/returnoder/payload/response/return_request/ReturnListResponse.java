package com.example.storesports.core.client.returnoder.payload.response.return_request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnListResponse {
    private String orderCode;
    private String requestDate;
    private String note;
    private int totalProduct;
    private List<ReturnListDetailResponse> returnListDetailResponses;
}
