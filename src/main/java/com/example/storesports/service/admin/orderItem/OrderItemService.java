package com.example.storesports.service.admin.orderItem;

import java.util.List;

public interface OrderItemService {

    List<Object[]> getOrderItemByOrderCode(String orderCode);
}
