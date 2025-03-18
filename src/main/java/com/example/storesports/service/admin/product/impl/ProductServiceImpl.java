package com.example.storesports.service.admin.product.impl;
import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.image.cloudinary.CloudinaryService;
import com.example.storesports.service.admin.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    private final CloudinaryService cloudinaryService;

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
    public List<ProductResponse> getAllParentProduct() {
        List<Product> productList = productRepository.findAllParentProducts();
        return productList.stream().map(this::mapToResponse).collect(Collectors.toList());
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
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ErrorException("product is not found"));
        return mapToResponse(product);
    }


    @Override
    public List<ProductResponse> findByParentId(Long parentId) {
        List<Product> products = productRepository.findByParentProductId(parentId);
        return products.stream().map(this::mapToResponse).toList();
    }


    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createProductWithVariants(List<ProductRequest> requests, MultipartFile[] images) {
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("Danh sách yêu cầu sản phẩm trống!");
        }

        ProductRequest request = requests.get(0);
        Product parentProduct = createParentProduct(request);
        handleTags(request, parentProduct);

        // Upload và lưu ảnh cho sản phẩm cha
        List<String> parentImageUrls = uploadImages(request.getParentImages());
        if (!parentImageUrls.isEmpty()) {
            List<ProductImage> parentProductImages = parentImageUrls.stream()
                    .map(url -> {
                        ProductImage productImage = new ProductImage();
                        productImage.setImageUrl(url);
                        productImage.setProduct(parentProduct);
                        return productImage;
                    })
                    .collect(Collectors.toList());
            productImageRepository.saveAll(parentProductImages);
            parentProduct.getImages().addAll(parentProductImages);
            productRepository.save(parentProduct);
            System.out.println("✅ Đã gán " + parentProductImages.size() + " ảnh cho sản phẩm cha " + parentProduct.getSku() + ": " + parentImageUrls);
        }

        List<Set<String>> attributeValues = request.getProductAttributeValues().stream()
                .collect(Collectors.groupingBy(
                        ProductRequest.ProductAttributeValue::getAttributeId,
                        Collectors.mapping(ProductRequest.ProductAttributeValue::getValue, Collectors.toSet())
                )).values().stream().collect(Collectors.toList());

        List<List<String>> valueCombinations = generateCombinations(attributeValues);
        int expectedCombinations = attributeValues.stream().mapToInt(Set::size).reduce(1, (a, b) -> a * b);
        if (valueCombinations.size() != expectedCombinations) {
            throw new IllegalArgumentException("Số tổ hợp không khớp với số giá trị thuộc tính!");
        }

        if (request.getVariants().size() != valueCombinations.size()) {
            throw new IllegalArgumentException("Số biến thể (" + request.getVariants().size() + ") không khớp với số tổ hợp (" + valueCombinations.size() + ")!");
        }
        if (images != null && images.length > 0 && images.length < valueCombinations.size()) {
            throw new IllegalArgumentException("Số ảnh (" + images.length + ") không đủ cho " + valueCombinations.size() + " tổ hợp!");
        }

        // Gán ảnh cho các biến thể nếu không có ảnh riêng
        if (images != null && images.length > 0) {
            associateImagesWithVariants(request.getVariants(), images);
        }

        List<Product> childProducts = new ArrayList<>();
        List<ProductAttributeValue> attributeValuesList = new ArrayList<>();
        AtomicInteger variantIndex = new AtomicInteger(0);

        for (List<String> combination : valueCombinations) {
            ProductRequest.ProductVariant variant = request.getVariants().get(variantIndex.get());
            Product childProduct = new Product();
            String variantName = String.join(" - ", combination);
            childProduct.setName(request.getName() + " - " + variantName);
            childProduct.setDescription(request.getDescription());
            childProduct.setSportType(request.getSportType());
            childProduct.setPrice(variant.getPrice() != null ? variant.getPrice() : 0.0);
            childProduct.setStockQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
            childProduct.setParentProductId(parentProduct.getId());
            childProduct.setSupplier(parentProduct.getSupplier());
            childProduct.setCategory(parentProduct.getCategory());

            String sku = generateUniqueSku(request.getName(), request.getCategoryId(), request.getSupplierId());
            childProduct.setSku(sku);

            // Tải ảnh và thêm vào danh sách images của childProduct
            List<String> imageUrls = uploadImages(variant);
            List<ProductImage> childProductImages = new ArrayList<>();
            for (String imageUrl : imageUrls) {
                if (!imageUrl.isEmpty()) {
                    ProductImage productImage = new ProductImage();
                    productImage.setImageUrl(imageUrl);
                    productImage.setProduct(childProduct);
                    childProductImages.add(productImage);
                    childProduct.getImages().add(productImage);
                }
            }

            if (childProductImages.isEmpty() && !parentProduct.getImages().isEmpty()) {
                List<ProductImage> parentImages = parentProduct.getImages();
                for (ProductImage parentImage : parentImages) {
                    ProductImage childImage = new ProductImage();
                    childImage.setImageUrl(parentImage.getImageUrl());
                    childImage.setProduct(childProduct);
                    childProductImages.add(childImage);
                    childProduct.getImages().add(childImage);
                }
                System.out.println("⚠️ Không có ảnh riêng, sử dụng ảnh cha cho biến thể " + childProduct.getSku());
            }

            childProducts.add(childProduct);
            attributeValuesList.addAll(mapAttributesToValues(childProduct, combination, request.getProductAttributeValues()));
            variantIndex.incrementAndGet();
        }

        productRepository.saveAll(childProducts);
        productAttributeValueRepository.saveAll(attributeValuesList);

        for (Product child : childProducts) {
            handleTags(request, child);
        }
    }



    private Product createParentProduct(ProductRequest request) {
        Product parentProduct = new Product();
        parentProduct.setName(request.getName());
        parentProduct.setDescription(request.getDescription());
        parentProduct.setSportType(request.getSportType());
        parentProduct.setPrice(0.0);
        parentProduct.setStockQuantity(0);

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp không tồn tại"));
        parentProduct.setSupplier(supplier);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
        parentProduct.setCategory(category);

        parentProduct.setSku(generateUniqueSku(request.getName(), request.getCategoryId(), request.getSupplierId()));
        return productRepository.save(parentProduct);
    }

    private void handleTags(ProductRequest request, Product product) {
        if (request.getTagId() == null || request.getTagId().isEmpty()) {
            return;
        }

        List<ProductTag> tags = request.getTagId().stream()
                .map(tagId -> productTagRepository.findById(tagId)
                        .orElseThrow(() -> new IllegalArgumentException("Tag không tồn tại với ID: " + tagId)))
                .collect(Collectors.toList());

        productTagMappingRepository.deleteByProductId(product.getId());
        List<ProductTagMapping> mappings = tags.stream()
                .map(tag -> {
                    ProductTagMapping mapping = new ProductTagMapping();
                    mapping.setProduct(product);
                    mapping.setTag(tag);
                    return mapping;
                })
                .collect(Collectors.toList());
        productTagMappingRepository.saveAll(mappings);
    }

    private List<ProductAttributeValue> mapAttributesToValues(Product product, List<String> combination, List<ProductRequest.ProductAttributeValue> productAttributeValues) {
        List<ProductAttributeValue> values = new ArrayList<>();
        List<Long> attributeIds = productAttributeValues.stream()
                .map(ProductRequest.ProductAttributeValue::getAttributeId)
                .distinct()
                .collect(Collectors.toList());

        for (int i = 0; i < combination.size(); i++) {
            ProductAttribute attribute = productAttributeRepository.findById(attributeIds.get(i))
                    .orElseThrow(() -> new IllegalArgumentException("Thuộc tính không tồn tại"));
            ProductAttributeValue value = new ProductAttributeValue();
            value.setProduct(product);
            value.setAttribute(attribute);
            value.setValue(combination.get(i));
            values.add(value);
        }
        return values;
    }

    private void associateImagesWithVariants(List<ProductRequest.ProductVariant> variants, MultipartFile[] images) {
        int imageIndex = 0;
        for (ProductRequest.ProductVariant variant : variants) {
            if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                continue;
            }
            List<MultipartFile> variantImages = new ArrayList<>();
            if (imageIndex < images.length) {
                variantImages.add(images[imageIndex]);
                imageIndex++;
            }
            variant.setImages(variantImages);
        }
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadFile(image, "products");
                        imageUrls.add(imageUrl);
                    } catch (IOException e) {
                        log.error("Lỗi khi upload ảnh: {}", e.getMessage());
                        imageUrls.add("");
                    }
                }
            }
        }
        return imageUrls;
    }

    private List<String> uploadImages(ProductRequest.ProductVariant variant) {
        List<String> imageUrls = new ArrayList<>();
        if (variant.getImages() != null && !variant.getImages().isEmpty()) {
            for (MultipartFile image : variant.getImages()) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = cloudinaryService.uploadFile(image, "products");
                        imageUrls.add(imageUrl);
                    } catch (IOException e) {
                        log.error("Lỗi khi upload ảnh: {}", e.getMessage());
                        imageUrls.add("");
                    }
                }
            }
        }
        return imageUrls;
    }

    private String generateUniqueSku(String name, Long categoryId, Long supplierId) {
        String baseSku = name.substring(0, Math.min(name.length(), 3)).toUpperCase() +
                categoryId + supplierId + new Random().nextInt(1000);
        String sku = baseSku;
        int suffix = 1;
        while (productRepository.existsBySku(sku)) {
            sku = baseSku + "-" + suffix++;
        }
        return sku;
    }

    private List<List<String>> generateCombinations(List<Set<String>> attributeValues) {
        List<List<String>> combinations = new ArrayList<>();
        generateCombinationsHelper(attributeValues, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private void generateCombinationsHelper(List<Set<String>> attributeValues, int depth, List<String> current, List<List<String>> result) {
        if (depth == attributeValues.size()) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (String value : attributeValues.get(depth)) {
            current.add(value);
            generateCombinationsHelper(attributeValues, depth + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    public ProductResponse mapToResponse(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        ProductResponse response = new ProductResponse();
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

    //    private String extractVariantSuffix(Product child, String parentSku) {
//        String currentChildSku = child.getSku();
//        if (currentChildSku.startsWith(parentSku + "-")) {
//            return currentChildSku.substring(parentSku.length() + 1);
//        }
//        return currentChildSku;
//    }

//    private void handleImages(ProductRequest request, Product productSaved) {
//        List<Long> existingImageIds = productImageRepository.findByProductId(productSaved.getId())
//                .stream().map(ProductImage::getId).toList();
//
//        // Nếu danh sách hình ảnh mới giống danh sách cũ thì không cập nhật
//        if (request.getProductImageIds() != null && !existingImageIds.equals(request.getProductImageIds())) {
//            productImageRepository.deleteByProductId(productSaved.getId());
//            for (Long imageId : request.getProductImageIds()) {
//                ProductImage image = productImageRepository.findById(imageId)
//                        .orElseThrow(() -> new ErrorException("ProductImage not found with ID: " + imageId));
//                image.setProduct(productSaved);
//                productImageRepository.save(image);
//            }
//        }
//    }

//    private List<List<String>> generateCombinations(List<List<String>> lists) {
//        List<List<String>> result = new ArrayList<>();
//        result.add(new ArrayList<>());
//
//        for (List<String> values : lists) {
//            List<List<String>> temp = new ArrayList<>();
//            for (List<String> combination : result) {
//                for (String value : values) {
//                    List<String> newCombination = new ArrayList<>(combination);
//                    newCombination.add(value);
//                    temp.add(newCombination);
//                }
//            }
//            result = temp;
//        }
//        return result;
//    }


//    private void handleProductImages(List<Product> childProducts, ProductRequest productRequest) {
//        List<Long> productImageIds = Optional.ofNullable(productRequest.getProductImageIds()).orElse(Collections.emptyList());
//
//        if (!productImageIds.isEmpty()) {
//            List<ProductImage> productImages = productImageRepository.findAllById(productImageIds);
//
//            for (Product childProduct : childProducts) {
//                List<ProductImage> newProductImages = new ArrayList<>();
//
//                for (ProductImage img : productImages) {
//                    ProductImage newImg = new ProductImage();
//                    newImg.setProduct(childProduct);
//                    newImg.setImageUrl(img.getImageUrl()); // Sửa lỗi typo
//                    newProductImages.add(newImg);
//                }
//                productImageRepository.saveAll(newProductImages);
//            }
//        }
//    }


}
