package com.example.storesports.core.admin.return_request.response;

import com.example.storesports.core.admin.return_media.payload.ReturnMediaAdminResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestItemResponse {
    private Long id;
    private String productName;
    private String imageProduct;
    private int quantity;
    private String reason;
    private String note;
    private String status;
    private Double unitPrice;
    private Double totalRefundAmount;
    private Map<String, String> attributes;
   private List<ReturnMediaAdminResponse> returnMediaAdminResponses;
}
