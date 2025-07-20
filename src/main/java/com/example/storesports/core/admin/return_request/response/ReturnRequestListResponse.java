package com.example.storesports.core.admin.return_request.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRequestListResponse {

    private String userName;
    private String code; // Mã đơn hoàn (RRxxxx)
    private Date requestDate; // Ngày gửi yêu cầu
    private String note; // Ghi chú chung
    private Long totalProduct;
    private String thumbnailUrl;
}
