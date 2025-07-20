package com.example.storesports.core.client.returnoder.payload.request.return_request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestRequest {

    private Long orderId;

    private String code;

    private String note;

    private Date requestDate;

    private String bankAccountNumber;

    private String bankAccountName;

    private String bankName;

    private List<ReturnRequestItemRequest> items;
}
