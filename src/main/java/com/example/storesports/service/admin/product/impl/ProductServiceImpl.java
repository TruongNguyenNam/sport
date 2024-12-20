package com.example.storesports.service.admin.product.impl;
import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.exceptions.NotFoundException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
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
        Product product =  mapToProduct(request);
        Product productSaved = productRepository.save(product);
        handleSportTypes(request, productSaved);
        handleTags(request, productSaved);
        handleImages(request, productSaved);
        handleSpecifications(request, productSaved);
        updateInventory(productSaved);

        Product productWithRelations = Objects.requireNonNull(productRepository.findById(productSaved.getId()))
                .orElseThrow(() -> new ErrorException("Product not found after saving: " + productSaved.getId()));


        return  mapToResponse(productWithRelations);

    }

    @Override
    public Page<ProductResponse> searchProductsByAttribute(int page, int size, ProductSearchRequest productSearchRequest) {
        int validatedPage = PageUtils.validatePageNumber(page);
        int validatedSize = PageUtils.validatePageSize(size, 2);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Specification<Product> specification = Specification.where(null);

        if (productSearchRequest.getName() != null && !productSearchRequest.getName().isEmpty()) {
            specification = specification.and(ProductSpecification.findByName(productSearchRequest.getName()));
        }
        if (productSearchRequest.getSize() != null && !productSearchRequest.getSize().isEmpty()) {
            specification = specification.and(ProductSpecification.findBySize(productSearchRequest.getSize()));
        }
        if (productSearchRequest.getMaterial() != null && !productSearchRequest.getMaterial().isEmpty()) {
            specification = specification.and(ProductSpecification.findByMaterial(productSearchRequest.getMaterial()));
        }
        if (productSearchRequest.getSportType() != null && !productSearchRequest.getSportType().isEmpty()) {
            specification = specification.and(ProductSpecification.findBySportType(productSearchRequest.getSize()));
        }
        if (productSearchRequest.getColor() != null && !productSearchRequest.getColor().isEmpty()) {
            specification = specification.and(ProductSpecification.findByColor(productSearchRequest.getColor()));
        }
        if (productSearchRequest.getColor() != null && !productSearchRequest.getColor().isEmpty()) {
            specification = specification.and(ProductSpecification.findByColor(productSearchRequest.getColor()));
        }
        if (productSearchRequest.getSupplierName() != null && !productSearchRequest.getSupplierName().isEmpty()) {
            specification = specification.and(ProductSpecification.findBySupplierName(productSearchRequest.getSupplierName()));
        }
        if (productSearchRequest.getCategoryName() != null && !productSearchRequest.getCategoryName().isEmpty()) {
            specification = specification.and(ProductSpecification.findByCategoryName(productSearchRequest.getCategoryName()));
        }
        if (productSearchRequest.getMinPrice() != null && productSearchRequest.getMaxPrice() != null) {
            specification = specification.and(ProductSpecification.hasPriceRange(productSearchRequest.getMinPrice(),productSearchRequest.getMaxPrice()));
        }

        Page<Product> productPage = productRepository.findAll(specification, pageable);

        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(productResponses, pageable, productPage.getTotalElements());
    }

    @Override
    @Transactional
    public void delete(List<Long> id) {
       List<Product> productList = productRepository.findAllById(id);
       if(!productList.isEmpty()){
           productRepository.deleteAllInBatch(productList);
       }
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(ProductRequest request, Long id) {
        Product product = Objects.requireNonNull(productRepository.findById(id))
                .orElseThrow(() -> new NotFoundException("Product not found"));
        updateAttribute(request,product);
        Product productSaved = productRepository.save(product);
        handleSportTypes(request, productSaved);
        handleTags(request, productSaved);
        handleImages(request, productSaved);
        handleSpecifications(request, productSaved);
        updateInventory(productSaved);
        Product productWithRelations = Objects.requireNonNull(productRepository.findById(productSaved.getId()))
                .orElseThrow(() -> new ErrorException("Product not found after saving: " + productSaved.getId()));

        return mapToResponse(productWithRelations);
    }

    private void updateAttribute(ProductRequest request, Product product){
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSportType(request.getSportType());
        product.setMaterial(request.getMaterial());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setSku(UUID.randomUUID().toString()); // UUID
        product.setSupplier(supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ErrorException("Supplier is not found: " + request.getSupplierId())));
        product.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ErrorException("Category is not found: " + request.getCategoryId())));
    }


    private Product mapToProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSportType(request.getSportType());
        product.setMaterial(request.getMaterial());
        product.setSize(request.getSize());
        product.setColor(request.getColor());
        product.setSku(UUID.randomUUID().toString()); // UUID
        product.setSupplier(supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ErrorException("Supplier is not found: " + request.getSupplierId())));
        product.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ErrorException("Category is not found: " + request.getCategoryId())));
        return product;
    }

    private void handleSportTypes(ProductRequest request, Product productSaved) {
        productSportTypeMappingRepository.deleteByProductId(productSaved.getId());
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
    }


    private void handleTags(ProductRequest request, Product productSaved) {
        productTagMappingRepository.deleteByProductId(productSaved.getId());
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
    }

    private void handleImages(ProductRequest request, Product productSaved) {
        productImageRepository.deleteByProductId(productSaved.getId());
        if (request.getProductImageIds() != null && !request.getProductImageIds().isEmpty()) {
            for (Long imageId : request.getProductImageIds()) {
                ProductImage image = productImageRepository.findById(imageId)
                        .orElseThrow(() -> new ErrorException("ProductImage not found with ID: " + imageId));
                image.setProduct(productSaved);
                productImageRepository.save(image);
            }
        }
    }

    private void handleSpecifications(ProductRequest request, Product productSaved) {
        productSpecificationOptionRepository.deleteByProductId(productSaved.getId());
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
    }

    private void updateInventory(Product productSaved) {
        Inventory inventory = inventoryRepository.findByProductId(productSaved.getId()).orElse(new Inventory());
        inventory.setProduct(productSaved);
        inventory.setStockQuantity(productSaved.getStockQuantity());
        inventoryRepository.save(inventory);
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
