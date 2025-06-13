package com.example.storesports.service.client.product.impl;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.client.product.payload.ProductResponseClient;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductImage;
import com.example.storesports.repositories.*;
import com.example.storesports.service.client.product.ProductClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClientServiceImpl implements ProductClientService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final SupplierRepository supplierRepository;

    private final ProductImageRepository productImageRepository;

    private final ProductTagRepository productTagRepository;

    private final ProductTagMappingRepository productTagMappingRepository;

    private final ProductAttributeRepository productAttributeRepository;

    private final ProductAttributeValueRepository productAttributeValueRepository;

    private final InventoryRepository inventoryRepository;

    @Override
    public List<ProductResponseClient> getAllProduct() {
        List<Product> productList = productRepository.findAllChildProduct();
        if(productList.isEmpty()){
            throw new IllegalArgumentException("danh sách sản phẩm bị trống");
        }

        return productList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }



    public ProductResponseClient mapToResponse(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        ProductResponseClient response = new ProductResponseClient();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
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


        response.setTagName(productTagMappingRepository.findByProductId(product.getId())
                .stream().map(productTagMapping ->
                        productTagMapping.getTag().getName()).collect(Collectors.toList()));


        response.setImageUrl(productImageRepository.findByProductId(product.getId())
                .stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));

        response.setProductAttributeValueResponses(
                productAttributeValueRepository
                        .findByProductId(product.getId()).stream()
                        .map(productAttributeValue -> {
                            ProductResponseClient.ProductAttributeValueResponse optionResponse = new ProductResponseClient.ProductAttributeValueResponse();
                            optionResponse.setId(productAttributeValue.getId());
                            optionResponse.setAttributeId(productAttributeValue.getAttribute().getId());
                            optionResponse.setAttributeName(productAttributeValue.getAttribute().getName());
                            optionResponse.setProductId(productAttributeValue.getProduct().getId());
                            optionResponse.setValue(productAttributeValue.getValue());
                            return optionResponse;
                        })
                        .collect(Collectors.toList())
        );

//        response.setInventories(
//                inventoryRepository.findByProductId(product.getId()).stream()
//                        .map(inventory -> {
//                            ProductResponse.InventoryResponse inventoryResponse = new ProductResponse.InventoryResponse();
//                            inventoryResponse.setId(inventory.getId());
//                            inventoryResponse.setProductName(inventory.getProduct().getName());
//                            inventoryResponse.setStockQuantity(String.valueOf(inventory.getStockQuantity()));
//                            return inventoryResponse;
//                        })
//                        .collect(Collectors.toList())
//        );

        return response;
    }

}
