package com.example.storesports.service.client.shopping_cart;

import com.example.storesports.core.client.shopping_cart.payload.OrderRequestClient;
import com.example.storesports.core.client.shopping_cart.payload.OrderResponseClient;
import com.example.storesports.core.client.shopping_cart.payload.ShoppingCartRequest;
import com.example.storesports.core.client.shopping_cart.payload.ShoppingCartResponse;
import com.example.storesports.infrastructure.constant.OrderStatus;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ShoppingCartService {
    ShoppingCartResponse addToCart(ShoppingCartRequest request);

    List<ShoppingCartResponse> viewToCart(Long userId);

    void removeProductWithCart(Long id);

    ShoppingCartResponse updateCartQuantity(Long id, Integer newQuantity);

    long countCartItemsByUserId(Long userId);

    OrderResponseClient checkoutv2(OrderRequestClient request);

    OrderResponseClient checkout(OrderRequestClient request, HttpServletRequest httpServletRequest);

    List<OrderResponseClient> findByCustomerId(Long customerId);

    OrderResponseClient updateOrderStatus(String orderCode);

}


