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

    private final ProductImageRepository productImageRepository;

    private final  ProductTagRepository productTagRepository;

    private final  ProductTagMappingRepository productTagMappingRepository;

    private final ProductAttributeRepository productAttributeRepository;

    private final ProductAttributeValueRepository productAttributeValueRepository;

    private final InventoryRepository inventoryRepository;

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size) {
        int validatedPage = PageUtils.validatePageNumber(page);
        int validatedSize = PageUtils.validatePageSize(size, 2);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Page<Product> productPage  = productRepository.findAll(pageable);
        if(productPage.isEmpty()){
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<ProductResponse> productResponses = productPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(productResponses, pageable, productPage.getTotalElements());
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

        if (productSearchRequest.getSportType() != null && !productSearchRequest.getSportType().isEmpty()) {
            specification = specification.and(ProductSpecification.findBySportType(productSearchRequest.getSportType()));
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
        if (!productList.isEmpty()) {
            // Xóa hình ảnh liên quan đến sản phẩm trước
            for (Product product : productList) {
                productImageRepository.deleteByProductId(product.getId());
                productTagMappingRepository.deleteByProductId(product.getId());
                inventoryRepository.deleteByProductId(product.getId());
                productAttributeValueRepository.deleteByProductId(product.getId());
            }

            // Sau đó xóa sản phẩm
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
        handleTags(request, productSaved);
        handleImages(request, productSaved);
        handleSpecifications(request, productSaved);
        updateInventory(productSaved);
        Product productWithRelations = Objects.requireNonNull(productRepository.findById(productSaved.getId()))
                .orElseThrow(() -> new ErrorException("Product not found after saving: " + productSaved.getId()));

        return mapToResponse(productWithRelations);
    }

    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ErrorException("product is not found"));
        return mapToResponse(product);
    }

    private void updateAttribute(ProductRequest request, Product product){
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSportType(request.getSportType());
        product.setSku(request.getSku()); // UUID
        product.setSupplier(supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ErrorException("Supplier is not found: " + request.getSupplierId())));
        product.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ErrorException("Category is not found: " + request.getCategoryId())));
    }

    @Override
    @Transactional
    public ProductResponse addNewProduct(ProductRequest request) {
        Product product =  mapToProduct(request);
        Product productSaved = productRepository.save(product);
        handleTags(request, productSaved);
        handleImages(request, productSaved);
        handleSpecifications(request, productSaved);
        updateInventory(productSaved);

        Product productWithRelations = Objects.requireNonNull(productRepository.findById(productSaved.getId()))
                .orElseThrow(() -> new ErrorException("Product not found after saving: " + productSaved.getId()));


        return  mapToResponse(productWithRelations);

    }

    private Product mapToProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSportType(request.getSportType());
        product.setSku(generateSKU(request)); // UUID
        product.setSupplier(supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ErrorException("Supplier is not found: " + request.getSupplierId())));
        product.setCategory(categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ErrorException("Category is not found: " + request.getCategoryId())));
        return product;
    }

    private String generateSKU(ProductRequest request) {
        // Nếu SKU được truyền từ request, sử dụng làm cơ sở
        String baseSku = (request.getSku() != null && !request.getSku().isEmpty())
                ? request.getSku().trim()
                : "SKU01";

        // Danh sách kết hợp để tạo SKU
        List<String> combination = new ArrayList<>();

        // Thêm các giá trị thuộc tính sản phẩm
        if (request.getProductAttributeValues() != null) {
            for (ProductRequest.ProductAttributeValue attributeValue : request.getProductAttributeValues()) {
                if (attributeValue.getValue() != null && !attributeValue.getValue().isEmpty()) {
                    combination.add(attributeValue.getValue().trim().toUpperCase());
                }
            }
        }

        // Ghép các thành phần lại với nhau
        return buildSku(baseSku, combination);
    }


    private String buildSku(String baseSku, List<String> combination) {
        StringBuilder skuBuilder = new StringBuilder(baseSku);
        combination.forEach(value -> skuBuilder.append("-").append(value.trim()));
        return skuBuilder.toString();
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
//        productImageRepository.deleteByProductId(productSaved.getId());
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
        productAttributeValueRepository.deleteByProductId(productSaved.getId());
        if (request.getProductAttributeValues() != null && !request.getProductAttributeValues().isEmpty()) {
            for (ProductRequest.ProductAttributeValue value : request.getProductAttributeValues()) {
                ProductAttributeValue productAttributeValue = new ProductAttributeValue();
                productAttributeValue.setProduct(productSaved);
                productAttributeValue.setAttribute(productAttributeRepository.findById(value.getAttributeId())
                        .orElseThrow(() -> new ErrorException("Attribute not found with Ì:" + value.getAttributeId())));
                productAttributeValue.setValue(value.getValue());
                productAttributeValueRepository.save(productAttributeValue);
            }
        }
    }

    private void updateInventory(Product productSaved) {
        Inventory inventory = inventoryRepository.findByProductId(productSaved.getId()).orElse(null);

        if (inventory != null) {
            // Nếu tồn kho hiện tại tồn tại, xóa nó
            inventoryRepository.deleteByProductId(productSaved.getId());
        }

        // Tạo hoặc cập nhật tồn kho mới
        Inventory newInventory = new Inventory();
        newInventory.setProduct(productSaved);
        newInventory.setStockQuantity(productSaved.getStockQuantity());
        inventoryRepository.save(newInventory);
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
                            ProductResponse.ProductAttributeValueResponse optionResponse = new ProductResponse.ProductAttributeValueResponse();
                            optionResponse.setId(productAttributeValue.getId());
                            optionResponse.setAttributeName(productAttributeValue.getAttribute().getName());
                            optionResponse.setProductId(productAttributeValue.getProduct().getId());
                            optionResponse.setValue(productAttributeValue.getValue());
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






}
