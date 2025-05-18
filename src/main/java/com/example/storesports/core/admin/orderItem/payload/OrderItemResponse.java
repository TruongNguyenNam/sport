package com.example.storesports.core.admin.orderItem.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double unitPrice;

    public OrderItemResponse(Long id, Long id1, String name, Integer quantity, Double unitPrice) {

    }
}
