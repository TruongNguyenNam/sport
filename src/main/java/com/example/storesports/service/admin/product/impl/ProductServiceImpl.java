package com.example.storesports.service.admin.product.impl;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.entity.Product;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.ProductRepository;
import com.example.storesports.service.admin.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size) {
        int validatedPage = PageUtils.validatePageNumber(page);
        int validatedSize = PageUtils.validatePageSize(size, 2);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Page<Product> productPage  = productRepository.findAll(pageable);
        if(productPage.isEmpty()){
//            throw new ErrorException("There are no products in the list yet" + productPage);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(productResponses, pageable, productPage.getTotalElements());
    }

    @Override
    public ProductResponse add(ProductRequest request) {
        return null;
    }

    public ProductResponse mapToResponse(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        ProductResponse response = new ProductResponse();
        // Ánh xạ các thuộc tính đơn giản
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSportType(product.getSportType());
        response.setMaterial(product.getMaterial());
        response.setSize(product.getSize());
        response.setColor(product.getColor());
        response.setSku(product.getSku());

        // Ánh xạ thuộc tính phức tạp
        if (product.getSupplier() != null) {
            response.setSupplierName(product.getSupplier().getName());
        }
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }

        // Map danh sách sportTypeName
        if (product.getProductSportTypeMappings() != null) {
            response.setSportTypeName(product.getProductSportTypeMappings().stream()
                    .map(mapping -> mapping.getSportType().getName())
                    .collect(Collectors.toList()));
        }

        // Map danh sách tagName
        if (product.getProductTagMappings() != null) {
            response.setTagName(product.getProductTagMappings().stream()
                    .map(mapping -> mapping.getTag().getName())
                    .collect(Collectors.toList()));
        }

        // Map danh sách ImageUrl
        if (product.getProductImages() != null) {
            response.setImageUrl(product.getProductImages().stream()
                    .map(image -> image.getImageUrl())
                    .collect(Collectors.toList()));
        }

//        if(product.getProductImages() != null){
//            response.setImageUrl(product.getProductImages().stream().map(productImage -> productImage.getImageUrl()).collect(Collectors.toList()));
//        }

        // Map danh sách productSpecificationOptionResponses
        if (product.getProductSpecificationOptions() != null) {
            response.setProductSpecificationOptionResponses(product.getProductSpecificationOptions().stream()
                    .map(option -> {
                        ProductResponse.ProductSpecificationOptionResponse optionResponse = new ProductResponse.ProductSpecificationOptionResponse();
                        optionResponse.setId(option.getId());
                        optionResponse.setSpecificationName(option.getSpecification().getName());
                        optionResponse.setProductId(option.getProduct().getId());
                        optionResponse.setValue(option.getValue());
                        return optionResponse;
                    }).collect(Collectors.toList()));
        }

        // Map danh sách inventories
        if (product.getInventories() != null) {
            response.setInventories(product.getInventories().stream()
                    .map(inventory -> {
                        ProductResponse.InventoryResponse inventoryResponse = new ProductResponse.InventoryResponse();
                        inventoryResponse.setId(inventory.getId());
                        inventoryResponse.setProductName(inventory.getProduct().getName());
                        inventoryResponse.setStockQuantity(String.valueOf(inventory.getStockQuantity()));
                        return inventoryResponse;
                    }).collect(Collectors.toList()));
        }

        return response;
    }
}
