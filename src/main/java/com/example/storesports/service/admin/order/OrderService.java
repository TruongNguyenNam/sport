package com.example.storesports.service.admin.order;

import com.example.storesports.core.admin.order.payload.CreateInvoiceRequest;
import com.example.storesports.core.admin.order.payload.OrderRequest;
import com.example.storesports.core.admin.order.payload.OrderResponse;

import java.util.List;

public interface OrderService {

     OrderResponse createInvoice(CreateInvoiceRequest request);
     OrderResponse addProductToOrder(OrderRequest request);
     OrderResponse addOrderDetails(String orderCode, OrderRequest request);

     List<OrderResponse> getAll();

}
