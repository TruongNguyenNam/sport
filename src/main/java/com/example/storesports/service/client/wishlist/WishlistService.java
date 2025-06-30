package com.example.storesports.service.client.wishlist;

import com.example.storesports.core.client.wishlist.payload.WishlistRequest;
import com.example.storesports.core.client.wishlist.payload.WishlistResponse;

import java.util.List;

public interface WishlistService {

    WishlistResponse addToWishlist(WishlistRequest request);

    List<WishlistResponse> findByUserWishlist(Long userId);

    void removeFromWishlist(Long id);



}
