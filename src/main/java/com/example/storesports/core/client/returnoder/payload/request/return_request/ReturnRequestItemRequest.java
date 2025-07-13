package com.example.storesports.core.client.returnoder.payload.request.return_request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestItemRequest {
    private Long orderItemId;
    private Integer quantity;
    private String reason;
    private String note;
}
