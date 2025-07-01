package com.example.storesports.core.client.wishlist.payload;

import com.example.storesports.entity.Product;
import com.example.storesports.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class WishlistRequest {

    private Long userId;

    private Long productId;

    private Date addedDate;

    private Boolean deleted;

}
