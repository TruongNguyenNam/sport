package com.example.storesports.core.admin.order.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
    public class MonthlyOrderTypeResponse {
    private Integer month;
    private Long posOrders;   // bán tại chỗ (is_pos = true)
    private Long shipOrders;
}
