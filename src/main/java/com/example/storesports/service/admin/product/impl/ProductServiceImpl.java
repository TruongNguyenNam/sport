package com.example.storesports.service.admin.product.impl;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final SupplierRepository supplierRepository;

    private final SportTypeRepository sportTypeRepository;

    private final ProductImageRepository productImageRepository;

    private final  ProductTagRepository productTagRepository;

    private final  ProductSportTypeMappingRepository productSportTypeMappingRepository;

    private final  ProductTagMappingRepository productTagMappingRepository;

    private final ProductSpecificationRepository productSpecificationRepository;

    private final ProductSpecificationOptionRepository productSpecificationOptionRepository;

    private final InventoryRepository inventoryRepository;

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
    @Transactional
    public ProductResponse addNewProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSportType(request.getSportType());
        product.setMaterial(request.getMaterial());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setSku(request.getSku());
        product.setSupplier(supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ErrorException("supplier is not found" + request.getSupplierId())));

        product.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ErrorException("supplier is not found" + request.getSupplierId())));

        Product productSaved = productRepository.save(product);

        if (request.getSportTypeId() != null && !request.getSportTypeId().isEmpty()) {
            for (Long sportTypeId : request.getSportTypeId()) {
                SportType sportType = sportTypeRepository.findById(sportTypeId)
                        .orElseThrow(() -> new ErrorException("SportType not found with ID: " + sportTypeId));
                ProductSportTypeMapping mapping = new ProductSportTypeMapping();
                mapping.setSportType(sportType);
                mapping.setProduct(productSaved);
                productSportTypeMappingRepository.save(mapping);
            }
        }

        if (request.getTagId() != null && !request.getTagId().isEmpty()) {
            for (Long tagId : request.getTagId()) {
                ProductTag tag = productTagRepository.findById(tagId)
                        .orElseThrow(() -> new ErrorException("Tag not found with ID: " + tagId));
                ProductTagMapping mapping = new ProductTagMapping();
                mapping.setProduct(productSaved);
                mapping.setTag(tag);
                productTagMappingRepository.save(mapping);
            }
        }


        if (request.getProductImageIds() != null && !request.getProductImageIds().isEmpty()) {
            for (Long imageId : request.getProductImageIds()) {
                ProductImage image = productImageRepository.findById(imageId)
                        .orElseThrow(() -> new ErrorException("ProductImage not found with ID: " + imageId));
                image.setProduct(productSaved);
                productImageRepository.save(image);
            }
        }


        if (request.getProductSpecificationOptions() != null && !request.getProductSpecificationOptions().isEmpty()) {
            for (ProductRequest.ProductSpecificationOption option : request.getProductSpecificationOptions()) {
                ProductSpecificationOption specificationOption = new ProductSpecificationOption();
                specificationOption.setProduct(productSaved);
                specificationOption.setSpecification(productSpecificationRepository.findById(option.getSpecificationId())
                        .orElseThrow(() -> new ErrorException("Specification not found with ID: " + option.getSpecificationId())));
                specificationOption.setValue(option.getValue());
                productSpecificationOptionRepository.save(specificationOption);
            }
        }

//        if (request.getInventoryIds() != null && !request.getInventoryIds().isEmpty()) {
//            for (Long inventoryId : request.getInventoryIds()) {
//                Inventory inventory = inventoryRepository.findById(inventoryId)
//                        .orElseThrow(() -> new ErrorException("Inventory not found with ID: " + inventoryId));
//                inventory.setProduct(productSaved);
//                inventoryRepository.save(inventory);
//            }
//        }

        Inventory inventory = inventoryRepository.findByProductId(productSaved.getId()).orElse(new Inventory());
        inventory.setProduct(productSaved);
        inventory.setStockQuantity(productSaved.getStockQuantity());
        inventoryRepository.save(inventory);

//        List<Inventory> inventories = inventoryRepository.findAllByProductId(productSaved.getId());
//        if (inventories.isEmpty()) {
//            // Tạo mới nếu không tồn tại Inventory
//            Inventory newInventory = new Inventory();
//            newInventory.setProduct(productSaved);
//            newInventory.setStockQuantity(productSaved.getStockQuantity());
//            inventoryRepository.save(newInventory);
//        } else {
//            // Cập nhật các Inventory hiện có
//            for (Inventory inventory : inventories) {
//                inventory.setStockQuantity(productSaved.getStockQuantity());
//                inventoryRepository.save(inventory);
//            }
//        }

        Product productWithRelations = Objects.requireNonNull(productRepository.findById(productSaved.getId()))
                .orElseThrow(() -> new ErrorException("Product not found after saving: " + productSaved.getId()));


        return  mapToResponse(productWithRelations);

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

        if (product.getSupplier() != null) {
            response.setSupplierName(product.getSupplier().getName());
        }
        if (product.getCategory() != null) {
            response.setCategoryName(product.getCategory().getName());
        }

//        if (product.getProductSportTypeMappings() != null) {
//            response.setSportTypeName(product.getProductSportTypeMappings().stream()
//                    .map(mapping -> mapping.getSportType().getName())
//                    .collect(Collectors.toList()));
//        }

        response.setSportTypeName(productSportTypeMappingRepository.findByProductId(product.getId())
                .stream().map(productSportTypeMapping ->
                productSportTypeMapping.getSportType().getName()).collect(Collectors.toList()));


//        if (product.getProductTagMappings() != null) {
//            response.setTagName(product.getProductTagMappings().stream()
//                    .map(mapping -> mapping.getTag().getName())
//                    .collect(Collectors.toList()));
//        }

        response.setTagName(productTagMappingRepository.findByProductId(product.getId())
                .stream().map(productTagMapping ->
                  productTagMapping.getTag().getName()).collect(Collectors.toList()));



//        if (product.getProductImages() != null) {
//            response.setImageUrl(product.getProductImages().stream()
//                    .map(ProductImage::getImageUrl)
//                    .collect(Collectors.toList()));
//        }

        response.setImageUrl(productImageRepository.findByProductId(product.getId())
                .stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));

        response.setProductSpecificationOptionResponses(
                productSpecificationOptionRepository
                        .findByProductId(product.getId()).stream()
                        .map(productSpecificationOption -> {
                            ProductResponse.ProductSpecificationOptionResponse optionResponse = new ProductResponse.ProductSpecificationOptionResponse();
                            optionResponse.setId(productSpecificationOption.getId());
                            optionResponse.setSpecificationName(productSpecificationOption.getSpecification().getName());
                            optionResponse.setProductId(productSpecificationOption.getProduct().getId());
                            optionResponse.setValue(productSpecificationOption.getValue());
                            return optionResponse;
                        })
                        .collect(Collectors.toList())
        );




        response.setInventories(
                inventoryRepository.findByProductId(product.getId()).stream()
                        .map(inventory -> {
                            ProductResponse.InventoryResponse inventoryResponse = new ProductResponse.InventoryResponse();
                            inventoryResponse.setId(inventory.getId());
                            inventoryResponse.setProductName(inventory.getProduct().getName());
                            inventoryResponse.setStockQuantity(String.valueOf(inventory.getStockQuantity()));
                            return inventoryResponse;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }


//    public ProductRequest.ProductSpecificationOption mapToResponse(ProductSpecification productSpecification){
//        ProductRequest.ProductSpecificationOption option = new ProductRequest.ProductSpecificationOption();
//
//        option.setProductId();
//
//
//    }




}
