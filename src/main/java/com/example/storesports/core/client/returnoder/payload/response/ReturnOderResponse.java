package com.example.storesports.core.client.returnoder.payload.response;

import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnOderResponse {
    private Long oderId;

    private String code;

    private String status;

    private Date orderDate;

    private Double orderTotal;

    private List<ReturnProductResponse> productResponses;





}
