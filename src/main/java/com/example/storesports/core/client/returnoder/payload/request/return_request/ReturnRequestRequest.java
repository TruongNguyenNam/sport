package com.example.storesports.core.client.returnoder.payload.request.return_request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestRequest {
    private Long orderId;

    private String note;

    private Date requestDate;

    private List<ReturnRequestItemRequest> items;
}
