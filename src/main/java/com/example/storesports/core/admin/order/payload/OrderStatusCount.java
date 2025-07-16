package com.example.storesports.core.admin.order.payload;

import com.example.storesports.infrastructure.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCount {

    private OrderStatus orderStatus;
    private Long count;

}
