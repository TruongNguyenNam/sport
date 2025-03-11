package com.example.storesports.service.admin.product.impl;
import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.exceptions.NotFoundException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.*;
import com.example.storesports.service.admin.image.ProductImageService;
import com.example.storesports.service.admin.product.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
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

    private final ProductImageService productImageService;

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
    @Transactional
    public ProductResponse updateProduct(ProductRequest request, Long id) {
        Product parent = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (request.getChildProductId() == null) {
            return updateParentOnly(request, parent); // TH1: Cập nhật sản phẩm cha
        } else {
            return updateChildProduct(request, parent, request.getChildProductId()); // TH2: Cập nhật sản phẩm con
        }
    }

    private ProductResponse updateParentOnly(ProductRequest request, Product parent) {
        if (request.getName() != null) {
            parent.setName(request.getName());
        }
        if (request.getDescription() != null) {
            parent.setDescription(request.getDescription());
        }
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Nhà cung cấp không tồn tại"));
            parent.setSupplier(supplier);
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));
            parent.setCategory(category);
        }
        if (request.getSku() != null) {
            parent.setSku(request.getSku());
        }

        // Lưu sản phẩm cha trước khi xử lý tag
        productRepository.save(parent);

        // Xử lý tag cho sản phẩm cha
        if (request.getTagId() != null) {
            handleTags(request, parent);
        }

        // Đồng bộ sản phẩm con
        if (request.getProductAttributeValues() != null && !request.getProductAttributeValues().isEmpty()) {
            updateChildProductsOnParentChange(parent, request);
        } else {
            updateChildNamesAndSkus(parent);
        }

        return mapToResponse(parent);
    }

    private void updateChildNamesAndSkus(Product parent) {
        List<Product> children = productRepository.findByParentProductId(parent.getId());
        for (Product child : children) {
            String variantSuffix = extractVariantSuffix(child, parent.getSku());
            child.setName(parent.getName() + "-" + variantSuffix);
            child.setSku(parent.getSku() + "-" + variantSuffix);
        }
        productRepository.saveAll(children);
    }

    private void updateChildProductsOnParentChange(Product parent, ProductRequest request) {
        Map<Long, List<String>> attributeMap = buildAttributeMap(request.getProductAttributeValues());
        List<List<String>> combinations = generateCombinations(new ArrayList<>(attributeMap.values()));

        List<Product> existingChildren = productRepository.findByParentProductId(parent.getId());
        Map<String, Product> existingChildMap = existingChildren.stream()
                .collect(Collectors.toMap(Product::getSku, Function.identity()));

        List<Product> updatedChildren = new ArrayList<>();
        for (List<String> combo : combinations) {
            String variantSuffix = String.join("-", combo);
            String childSku = parent.getSku() + "-" + variantSuffix;

            Product child = existingChildMap.getOrDefault(childSku, new Product());
            child.setName(parent.getName() + "-" + variantSuffix);
            child.setSku(childSku);
            child.setDescription(parent.getDescription());
            child.setPrice(parent.getPrice());
            child.setStockQuantity(parent.getStockQuantity());
            child.setSportType(parent.getSportType());
            child.setSupplier(parent.getSupplier());
            child.setCategory(parent.getCategory());
            child.setParentProductId(parent.getId());
            updatedChildren.add(child);
        }

        // Lưu tất cả sản phẩm con trước
        productRepository.saveAll(updatedChildren);

        // Cập nhật thuộc tính và ảnh sau khi tất cả child đã được persist
        int index = 0;
        for (List<String> combo : combinations) {
            Product child = updatedChildren.get(index);
            updateChildAttributes(child, attributeMap, combo);
//            if (request.getProductImageIds() != null && !request.getProductImageIds().isEmpty()) {
//                updateChildImage(child, request.getProductImageIds());
//            }
            index++;
        }

        for (Product child : updatedChildren) {
            handleTags(request, child);
        }

        existingChildMap.keySet().removeAll(updatedChildren.stream()
                .map(Product::getSku)
                .collect(Collectors.toSet()));
        if (!existingChildMap.isEmpty()) {
            productRepository.deleteAll(existingChildMap.values());
        }
    }

    private void updateChildAttributes(Product child, Map<Long, List<String>> attributeMap, List<String> combo) {
        List<ProductAttributeValue> existingAttributes = productAttributeValueRepository.findByProductId(child.getId());
        productAttributeValueRepository.deleteAll(existingAttributes);

        int attributeIndex = 0;
        for (Map.Entry<Long, List<String>> entry : attributeMap.entrySet()) {
            Long attributeId = entry.getKey();
            ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new EntityNotFoundException("Thuộc tính không tồn tại"));

            ProductAttributeValue attributeValue = new ProductAttributeValue();
            attributeValue.setAttribute(attribute);
            attributeValue.setProduct(child);
            attributeValue.setValue(combo.get(attributeIndex));
            productAttributeValueRepository.save(attributeValue);
            attributeIndex++;
        }
    }

    private ProductResponse updateChildProduct(ProductRequest request, Product parent, Long childProductId) {
        Product child = productRepository.findById(childProductId)
                .orElseThrow(() -> new EntityNotFoundException("Child product not found"));

        if (!child.getParentProductId().equals(parent.getId())) {
            throw new IllegalArgumentException("Child product does not belong to the specified parent");
        }

        updateChildProductDetails(request, child);

//        if (request.getProductImageIds() != null && !request.getProductImageIds().isEmpty()) {
//            updateChildImage(child, request.getProductImageIds());
//        }

        productRepository.save(child);
        return mapToResponse(parent);
    }

    private void updateChildProductDetails(ProductRequest request, Product child) {
        if (request.getPrice() != null) {
            child.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            child.setStockQuantity(request.getStockQuantity());
        }
        if (request.getDescription() != null) {
            child.setDescription(request.getDescription());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));
            child.setCategory(category);
        }
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Nhà cung cấp không tồn tại"));
            child.setSupplier(supplier);
        }
    }

    private void updateChildImage(Product child, List<Long> productImageIds) {
        // Nếu productImageIds là null hoặc rỗng, giữ nguyên ảnh cũ và không làm gì cả
        if (productImageIds == null || productImageIds.isEmpty()) {
            return;
        }

        // Lấy danh sách ảnh hiện tại của sản phẩm con
        List<ProductImage> existingImages = productImageRepository.findByProductId(child.getId());
        Map<Long, ProductImage> existingImageMap = existingImages.stream()
                .collect(Collectors.toMap(ProductImage::getId, Function.identity()));

        // Lấy danh sách ảnh mới từ productImageIds
        List<ProductImage> newImages = productImageRepository.findAllById(productImageIds);
        if (newImages.size() != productImageIds.size()) {
            throw new IllegalArgumentException("Một hoặc nhiều hình ảnh không tồn tại");
        }

        // Cập nhật danh sách ảnh mới
        List<ProductImage> updatedImages = new ArrayList<>();
        for (ProductImage image : newImages) {
            image.setProduct(child); // Gắn ảnh vào sản phẩm con
            updatedImages.add(image);
        }

        // Xóa các ảnh cũ không còn trong danh sách mới
        existingImageMap.keySet().removeAll(productImageIds);
        if (!existingImageMap.isEmpty()) {
            productImageRepository.deleteAll(existingImageMap.values());
        }

        // Lưu các ảnh mới hoặc cập nhật
        productImageRepository.saveAll(updatedImages);
    }



    @Override
    public List<ProductResponse> findByParentId(Long parentId) {
        List<Product> products = productRepository.findByParentProductId(parentId);
        return products.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public ProductResponse createProductWithVariants(ProductRequest productRequest) {
        Product parentProduct = createParentProduct(productRequest);
        handleTags(productRequest, parentProduct);
        List<Product> childProducts = createChildProducts(parentProduct, productRequest);
        for (Product child : childProducts) {
            handleTags(productRequest, child);
        }
        handleProductAttributes(childProducts, productRequest);
        handleProductImages(parentProduct, childProducts, productRequest);
        return mapToResponse(parentProduct);
    }

    private Product createParentProduct(ProductRequest productRequest) {
        Product parentProduct = new Product();
        parentProduct.setName(productRequest.getName());
        parentProduct.setDescription(productRequest.getDescription());
        parentProduct.setPrice(productRequest.getPrice());
        parentProduct.setStockQuantity(productRequest.getStockQuantity());
        parentProduct.setSportType(productRequest.getSportType());
        parentProduct.setSku(productRequest.getSku());

        Supplier supplier = supplierRepository.findById(productRequest.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Nhà cung cấp không tồn tại"));
        parentProduct.setSupplier(supplier);

        Category category = categoryRepository.findById(productRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Danh mục không tồn tại"));
        parentProduct.setCategory(category);

        return productRepository.save(parentProduct);
    }

    private void handleTags(ProductRequest request, Product productSaved) {
        if (request.getTagId() == null || request.getTagId().isEmpty()) {
            return; // Giữ nguyên tag cũ nếu không cung cấp tag mới
        }

        // Kiểm tra tất cả tag trước
        List<ProductTag> tags = request.getTagId().stream()
                .map(tagId -> productTagRepository.findById(tagId)
                        .orElseThrow(() -> new ErrorException("Tag not found with ID: " + tagId)))
                .toList();

        // Xóa và thêm mới chỉ khi tất cả tag hợp lệ
        productTagMappingRepository.deleteByProductId(productSaved.getId());
        List<ProductTagMapping> mappings = tags.stream()
                .map(tag -> {
                    ProductTagMapping mapping = new ProductTagMapping();
                    mapping.setProduct(productSaved);
                    mapping.setTag(tag);
                    return mapping;
                })
                .collect(Collectors.toList());
        productTagMappingRepository.saveAll(mappings);
    }
    private List<Product> createChildProducts(Product parentProduct, ProductRequest productRequest) {
        Map<Long, List<String>> attributeValueMap = buildAttributeMap(productRequest.getProductAttributeValues());
        List<List<String>> valueCombinations = generateCombinations(new ArrayList<>(attributeValueMap.values()));

        List<Product> childProducts = new ArrayList<>();
        for (List<String> combination : valueCombinations) {
            String variantSuffix = String.join("-", combination);
            String childSku = parentProduct.getSku() + "-" + variantSuffix;

            Product childProduct = new Product();
            childProduct.setName(parentProduct.getName() + "-" + variantSuffix);
            childProduct.setSku(childSku);
            childProduct.setDescription(parentProduct.getDescription());
            childProduct.setPrice(parentProduct.getPrice());
            childProduct.setStockQuantity(parentProduct.getStockQuantity());
            childProduct.setSportType(parentProduct.getSportType());
            childProduct.setParentProductId(parentProduct.getId());
            childProduct.setSupplier(parentProduct.getSupplier());
            childProduct.setCategory(parentProduct.getCategory());
            childProducts.add(childProduct);
        }
        return productRepository.saveAll(childProducts);
    }

    private void handleProductAttributes(List<Product> childProducts, ProductRequest productRequest) {
        Map<Long, List<String>> attributeValueMap = buildAttributeMap(productRequest.getProductAttributeValues());
        List<ProductAttributeValue> attributeValueEntities = new ArrayList<>();

        for (int i = 0; i < childProducts.size(); i++) {
            Product childProduct = childProducts.get(i);
            List<String> combination = generateCombinations(new ArrayList<>(attributeValueMap.values())).get(i);
            int attributeIndex = 0;

            for (Map.Entry<Long, List<String>> entry : attributeValueMap.entrySet()) {
                Long attributeId = entry.getKey();
                ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                        .orElseThrow(() -> new EntityNotFoundException("Thuộc tính không tồn tại"));

                ProductAttributeValue attributeValue = new ProductAttributeValue();
                attributeValue.setAttribute(attribute);
                attributeValue.setProduct(childProduct);
                attributeValue.setValue(combination.get(attributeIndex));
                attributeValueEntities.add(attributeValue);
                attributeIndex++;
            }
        }
        productAttributeValueRepository.saveAll(attributeValueEntities);
    }


    private void handleProductImages(Product parentProduct, List<Product> childProducts, ProductRequest productRequest) {
        // Xử lý ảnh cho sản phẩm cha
        List<MultipartFile> parentImages = Optional.ofNullable(productRequest.getParentImages())
                .orElse(Collections.emptyList());
        if (!parentImages.isEmpty()) {
            System.out.println("🟡 Ảnh sản phẩm cha nhận được: " + parentImages.size());
            List<Long> parentImageIds = productImageService.saveProductImage(parentImages);
            List<ProductImage> parentProductImages = productImageRepository.findAllById(parentImageIds);
            parentProductImages.forEach(image -> image.setProduct(parentProduct));
            productImageRepository.saveAll(parentProductImages);
            System.out.println("✅ Đã gán " + parentProductImages.size() + " ảnh cho sản phẩm cha " + parentProduct.getSku());
        }

        // Tạo map ảnh theo tổ hợp thuộc tính đầy đủ (value-attributeId)
        Map<String, List<MultipartFile>> variantImageMap = new HashMap<>();
        for (ProductRequest.ProductAttributeValue attr : productRequest.getProductAttributeValues()) {
            if (attr.getVariantImages() != null && !attr.getVariantImages().isEmpty()) {
                String key = attr.getValue() + "-" + attr.getAttributeId(); // Định dạng key: value-attributeId
                variantImageMap.put(key, attr.getVariantImages());
            }
        }
        System.out.println("🟡 Mapping ảnh biến thể: " + variantImageMap);

        // Gán ảnh cho từng sản phẩm con
        for (Product childProduct : childProducts) {
            String variantKey = extractVariantSuffix(childProduct, parentProduct.getSku());
            System.out.println("🟢 Xử lý ảnh cho sản phẩm con: " + childProduct.getSku());
            System.out.println("🔹 Variant Key: " + variantKey);

            List<MultipartFile> variantImages = variantImageMap.getOrDefault(variantKey, Collections.emptyList());
            if (!variantImages.isEmpty()) {
                List<Long> childImageIds = productImageService.saveProductImage(variantImages);
                List<ProductImage> childProductImages = productImageRepository.findAllById(childImageIds);
                childProductImages.forEach(image -> image.setProduct(childProduct));
                productImageRepository.saveAll(childProductImages);
                System.out.println("✅ Đã lưu " + childProductImages.size() + " ảnh riêng cho sản phẩm con: " + childProduct.getSku());
            } else {
                List<ProductImage> existingChildImages = productImageRepository.findByProductId(childProduct.getId());
                if (existingChildImages.isEmpty()) {
                    // Chỉ sao chép ảnh từ sản phẩm cha nếu sản phẩm con chưa có ảnh
                    List<ProductImage> parentSavedImages = productImageRepository.findByProductId(parentProduct.getId());
                    List<ProductImage> childImages = parentSavedImages.stream()
                            .map(parentImage -> {
                                ProductImage childImage = new ProductImage();
                                childImage.setProduct(childProduct);
                                childImage.setImageUrl(parentImage.getImageUrl());
                                return childImage;
                            })
                            .collect(Collectors.toList());
                    productImageRepository.saveAll(childImages);
                    System.out.println("⚠️ Không tìm thấy ảnh biến thể, sử dụng ảnh sản phẩm cha cho: " + childProduct.getSku());
                } else {
                    System.out.println("⚠️ Sản phẩm con đã có ảnh, không sao chép từ sản phẩm cha: " + childProduct.getSku());
                }
            }
        }
    }



    private List<List<String>> generateCombinations(List<List<String>> lists) {
        List<List<String>> combinations = new ArrayList<>();
        generateCombinationsHelper(lists, 0, new ArrayList<>(), combinations);
        return combinations;
    }


    private void generateCombinationsHelper(
            List<List<String>> attributeValues,
            int currentDepth,
            List<String> currentCombination,
            List<List<String>> result
    ) {
        if (currentDepth == attributeValues.size()) {
            result.add(new ArrayList<>(currentCombination));
            return;
        }
        for (String value : attributeValues.get(currentDepth)) {
            currentCombination.add(value);
            generateCombinationsHelper(attributeValues, currentDepth + 1, currentCombination, result);
            currentCombination.remove(currentCombination.size() - 1);
        }
    }

    private String extractVariantSuffix(Product child, String parentSku) {
        String currentChildSku = child.getSku();
        if (currentChildSku.startsWith(parentSku + "-")) {
            String suffix = currentChildSku.substring(parentSku.length() + 1);
            // Giả sử SKU con có dạng: parentSku-value-attributeId (ví dụ: k-d-1)
            // Tách phần value và attributeId
            String[] parts = suffix.split("-");
            if (parts.length >= 2) {
                return parts[0] + "-" + parts[1]; // Trả về "d-1"
            }
            return suffix;
        }
        return currentChildSku;
    }

//    private String extractVariantSuffix(Product child, String parentSku) {
//        String currentChildSku = child.getSku();
//        if (currentChildSku.startsWith(parentSku + "-")) {
//            return currentChildSku.substring(parentSku.length() + 1);
//        }
//        return currentChildSku;
//    }

    private Map<Long, List<String>> buildAttributeMap(List<ProductRequest.ProductAttributeValue> attributeValues) {
        return attributeValues.stream()
                .collect(Collectors.groupingBy(
                        ProductRequest.ProductAttributeValue::getAttributeId,
                        Collectors.mapping(ProductRequest.ProductAttributeValue::getValue, Collectors.toList())
                ));
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
