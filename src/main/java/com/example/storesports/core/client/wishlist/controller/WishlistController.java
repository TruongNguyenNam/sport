package com.example.storesports.core.client.wishlist.controller;

import com.example.storesports.core.client.wishlist.payload.WishlistRequest;
import com.example.storesports.core.client.wishlist.payload.WishlistResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.wishlist.WishlistService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/client/wishlist")
@Validated
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Endpoints for managing Wishlist")
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/add")
    public ResponseData<WishlistResponse> addToWishlist(@RequestBody WishlistRequest request) {
        try {
            WishlistResponse response = wishlistService.addToWishlist(request);
            return new ResponseData<>(200, "Thêm sản phẩm vào danh sách yêu thích thành công", response);
        } catch (IllegalArgumentException e) {
            return new ResponseData<>(400, e.getMessage());
        } catch (Exception e) {
            return new ResponseData<>(500, "Lỗi hệ thống");
        }
    }

    @GetMapping("/all/{userId}")
    public ResponseData<List<WishlistResponse>> getAllWishlist(@PathVariable("userId") Long userId) {
        try {
            List<WishlistResponse> responseList = wishlistService.findByUserWishlist(userId);
            return new ResponseData<>(200, "Lấy danh sách yêu thích thành công", responseList);
        } catch (IllegalArgumentException e) {
            return new ResponseData<>(400, e.getMessage());
        } catch (Exception e) {
            return new ResponseData<>(500, "Lỗi hệ thống");
        }
    }

    @DeleteMapping("/remove/{id}")
    public ResponseData<String> removeFromWishlist(@PathVariable Long id) {
        try {
            wishlistService.removeFromWishlist(id);
            return new ResponseData<>(200, "Xóa sản phẩm khỏi danh sách yêu thích thành công", "Đã xóa");
        } catch (IllegalArgumentException e) {
            return new ResponseData<>(400, e.getMessage());
        } catch (Exception e) {
            return new ResponseData<>(500, "Lỗi hệ thống");
        }
    }






}
