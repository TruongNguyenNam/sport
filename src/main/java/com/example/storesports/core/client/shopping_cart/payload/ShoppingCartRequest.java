package com.example.storesports.core.client.shopping_cart.payload;

import com.example.storesports.entity.Product;
import com.example.storesports.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartRequest {

    private Long userId;

    private Long productId;

    private Integer quantity;

}
