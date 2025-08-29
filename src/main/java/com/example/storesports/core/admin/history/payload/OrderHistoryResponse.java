package com.example.storesports.core.admin.history.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class OrderHistoryResponse {
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String orderStatus;
    private String note;
    private String username;

}
