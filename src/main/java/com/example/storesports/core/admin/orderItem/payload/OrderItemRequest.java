package com.example.storesports.core.admin.orderItem.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
}
