package com.example.storesports.service.client.wishlist.impl;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.client.wishlist.payload.WishlistRequest;
import com.example.storesports.core.client.wishlist.payload.WishlistResponse;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductImage;
import com.example.storesports.entity.User;
import com.example.storesports.entity.Wishlist;
import com.example.storesports.repositories.*;
import com.example.storesports.service.client.wishlist.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final SupplierRepository supplierRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductTagRepository productTagRepository;

    private final ProductTagMappingRepository productTagMappingRepository;

    private final ProductAttributeRepository productAttributeRepository;

    private final ProductAttributeValueRepository productAttributeValueRepository;

    private final InventoryRepository inventoryRepository;
    @Override
    public WishlistResponse addToWishlist(WishlistRequest request) {
        Wishlist wishlist = new Wishlist();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("không tim thấy user này"));
        wishlist.setUser(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("không tim thấy sản phẩm này"));
        if (product.getParentProductId() == null) {
            throw new IllegalArgumentException("Chỉ có thể thêm sản phẩm con vào wishlist!");
        }

        wishlist.setProduct(product);
        wishlist.setAddedDate(request.getAddedDate() != null ? request.getAddedDate() : new Date());
        wishlist.setDeleted(request.getDeleted() != null ? request.getDeleted() : false);

        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        return mapToResponse(savedWishlist);
    }

    @Override
    public List<WishlistResponse> findByUserWishlist(Long userId){
        List<Wishlist> wishlists = wishlistRepository.findByUserIdAndDeletedFalse(userId);
        if(wishlists.isEmpty()){
            throw new IllegalArgumentException("không có sản phẩm trong danh sách yếu thích");
        }

        return wishlists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void removeFromWishlist(Long id) {
        Wishlist wishlist = wishlistRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("không tìm thấy ")
        );

            wishlist.setDeleted(true);
        wishlistRepository.save(wishlist);
    }

    private WishlistResponse mapToResponse(Wishlist wishlist) {
        WishlistResponse response = new WishlistResponse();
        response.setId(wishlist.getId());
        if (wishlist.getUser() != null) {
            response.setUserName(wishlist.getUser().getUsername());
        }
        response.setAddedDate(wishlist.getAddedDate());
        response.setDeleted(wishlist.getDeleted());
//        if (wishlist.getProduct() != null) {
//            WishlistResponse.Product productResponse = mapToWishlistProductResponse(wishlist.getProduct());
//            response.setProduct(productResponse);
//        }


        if (wishlist.getProduct() != null) {
            List<WishlistResponse.Product> products = new ArrayList<>();
            products.add(mapToWishlistProductResponse(wishlist.getProduct()));
            response.setProduct(products);
        } else {
            response.setProduct(new ArrayList<>());
        }

        return response;
    }


    private WishlistResponse.Product mapToWishlistProductResponse(Product product) {
        WishlistResponse.Product response = new WishlistResponse.Product();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setParentProductId(product.getParentProductId());
        response.setOriginalPrice(product.getOriginalPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSportType(product.getSportType());
        response.setSku(product.getSku());

        if (product.getSupplier() != null) {
            response.setSupplierName(product.getSupplier().getName());
        }
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }

        // Ánh xạ tagName
        response.setTagName(productTagMappingRepository.findByProductId(product.getId())
                .stream()
                .map(productTagMapping -> productTagMapping.getTag().getName())
                .collect(Collectors.toList()));

        // Ánh xạ imageUrl
        response.setImageUrl(productImageRepository.findByProductId(product.getId())
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList()));

        // Ánh xạ productAttributeValueResponses
        response.setProductAttributeValueResponses(
                productAttributeValueRepository.findByProductId(product.getId())
                        .stream()
                        .map(productAttributeValue -> {
                            WishlistResponse.ProductAttributeValueResponse optionResponse =
                                    new WishlistResponse.ProductAttributeValueResponse();
                            optionResponse.setId(productAttributeValue.getId());
                            optionResponse.setAttributeId(productAttributeValue.getAttribute().getId());
                            optionResponse.setAttributeName(productAttributeValue.getAttribute().getName());
                            optionResponse.setProductId(productAttributeValue.getProduct().getId());
                            optionResponse.setValue(productAttributeValue.getValue());
                            return optionResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }




}
