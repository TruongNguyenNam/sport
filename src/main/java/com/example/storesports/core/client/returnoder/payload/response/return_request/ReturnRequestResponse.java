package com.example.storesports.core.client.returnoder.payload.response.return_request;

import com.example.storesports.core.client.returnoder.payload.request.return_request.ReturnRequestItemRequest;
import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
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
public class ReturnRequestResponse {
    private Long orderId;
    private Long userId;
    private String note;
    private Date requestDate;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankName;
    private List<ReturnRequestItemResponse> items;
    private List<ReturnMediaResponse> returnMediaResponses;
}
