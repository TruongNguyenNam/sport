package com.example.storesports.core.admin.order.payload;

import com.example.storesports.infrastructure.constant.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateOrderStatusRequest {
    private OrderStatus newStatus;
    private String nodes;
}
