package com.example.storesports.core.client.returnoder.payload.response.return_history;

import com.example.storesports.core.client.returnoder.return_media.payload.ReturnMediaResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnHistoryItemResponse {
    private String productName;
    private String imageProduct;
    private int quantity;
    private String reason;
    private String note;
    private String status;
    private Double unitPrice;
    private Double totalRefundAmount;
    private List<ReturnMediaResponse> returnMediaResponses;
    private Map<String, String> attributes;
}
