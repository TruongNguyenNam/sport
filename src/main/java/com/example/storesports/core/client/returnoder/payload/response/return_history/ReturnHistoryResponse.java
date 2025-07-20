package com.example.storesports.core.client.returnoder.payload.response.return_history;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnHistoryResponse {
    private String code; // Mã đơn hoàn (RRxxxx)
    private Date requestDate; // Ngày gửi yêu cầu
    private String bankAccountName; // Tên chủ tài khoản
    private String bankAccountNumber; // STK
    private String bankName; // Ngân hàng
    private String note; // Ghi chú chung
    private Long totalProduct;
    private String thumbnailUrl;
}
