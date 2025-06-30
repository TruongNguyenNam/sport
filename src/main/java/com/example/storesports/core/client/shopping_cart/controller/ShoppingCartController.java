package com.example.storesports.core.client.shopping_cart.controller;

import com.example.storesports.core.client.shopping_cart.payload.OrderRequestClient;
import com.example.storesports.core.client.shopping_cart.payload.OrderResponseClient;
import com.example.storesports.core.client.shopping_cart.payload.ShoppingCartRequest;
import com.example.storesports.core.client.shopping_cart.payload.ShoppingCartResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.shopping_cart.ShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/client/cart")
@Validated
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Endpoints for managing Cart")
@Slf4j
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @PostMapping("/checkout")
    public ResponseData<OrderResponseClient> checkout(@RequestBody OrderRequestClient request) {
        try {
            OrderResponseClient response = shoppingCartService.checkout(request);
            return new ResponseData<>(200, "Thanh toán thành công", response);
        } catch (Exception e) {
            log.error("Lỗi khi thanh toán đơn hàng", e);
            return new ResponseData<>(500, "Thanh toán thất bại");
        }
    }

    @PostMapping("/add")
    public ResponseData<ShoppingCartResponse> addToCart(@RequestBody @Valid ShoppingCartRequest request) {
        try {
            ShoppingCartResponse response = shoppingCartService.addToCart(request);
            return new ResponseData<>(200, "Thêm vào giỏ hàng thành công", response);
        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e);
            return new ResponseData<>(500, "Thêm sản phẩm thất bại");
        }
    }

    @GetMapping("/view")
    public ResponseData<List<ShoppingCartResponse>> viewToCart(@RequestParam("userId") Long userId) {
        try {
            List<ShoppingCartResponse> responseList = shoppingCartService.viewToCart(userId);
            return new ResponseData<>(200, "Lấy giỏ hàng thành công", responseList);
        } catch (Exception e) {
            log.error("Lỗi khi xem giỏ hàng", e);
            return new ResponseData<>(500, "Không thể lấy giỏ hàng");
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseData<Void> removeProductWithCart(@PathVariable Long id) {
        try {
            shoppingCartService.removeProductWithCart(id);
            return new ResponseData<>(200, "Xoá sản phẩm thành công");
        } catch (Exception e) {
            log.error("Lỗi khi xoá sản phẩm khỏi giỏ hàng", e);
            return new ResponseData<>(500, "Xoá sản phẩm thất bại");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseData<ShoppingCartResponse> updateCartQuantity(
            @PathVariable Long id,
            @RequestParam("quantity") Integer newQuantity) {
        try {
            ShoppingCartResponse response = shoppingCartService.updateCartQuantity(id, newQuantity);
            return new ResponseData<>(200, "Cập nhật số lượng thành công", response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật số lượng giỏ hàng", e);
            return new ResponseData<>(500, "Cập nhật số lượng thất bại");
        }
    }

    @GetMapping("/count")
    public ResponseData<Long> countCartItems(@RequestParam("userId") Long userId) {
        try {
            long count = shoppingCartService.countCartItemsByUserId(userId);
            return new ResponseData<>(200, "Đếm số sản phẩm trong giỏ thành công", count);
        } catch (Exception e) {
            log.error("Lỗi khi đếm sản phẩm trong giỏ hàng", e);
            return new ResponseData<>(500, "Lỗi khi đếm sản phẩm trong giỏ hàng");
        }
    }


}
