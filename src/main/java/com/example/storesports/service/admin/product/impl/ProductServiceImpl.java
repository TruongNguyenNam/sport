package com.example.storesports.service.admin.product.impl;
import com.example.storesports.core.admin.product.payload.*;
import com.example.storesports.entity.*;
import com.example.storesports.infrastructure.exceptions.AttributeValueDuplicate;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.exceptions.NameNotExists;
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

    private final ProductTagRepository productTagRepository;

    private final ProductTagMappingRepository productTagMappingRepository;

    private final ProductAttributeRepository productAttributeRepository;

    private final ProductAttributeValueRepository productAttributeValueRepository;

    private final InventoryRepository inventoryRepository;

    private final CloudinaryService cloudinaryService;

    @Override
    public List<ProductResponse> getAllParentProduct() {
        List<Product> productList = productRepository.findAllParentProducts();
        return productList.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> searchProduct(ProductSearchRequest productSearchRequest) {
        Specification<Product> specification = Specification.where(null);

        if (Optional.ofNullable(productSearchRequest.getName()).filter(name -> !name.isEmpty()).isPresent()) {
            specification = specification.and(ProductSpecification.findByName(productSearchRequest.getName()));
        }

        if (Optional.ofNullable(productSearchRequest.getSportType()).filter(type -> !type.isEmpty()).isPresent()) {
            specification = specification.and(ProductSpecification.findBySportType(productSearchRequest.getSportType()));
        }

        if (Optional.ofNullable(productSearchRequest.getSupplierName()).filter(name -> !name.isEmpty()).isPresent()) {
            specification = specification.and(ProductSpecification.findBySupplierName(productSearchRequest.getSupplierName()));
        }

        if (Optional.ofNullable(productSearchRequest.getCategoryName()).filter(name -> !name.isEmpty()).isPresent()) {
            specification = specification.and(ProductSpecification.findByCategoryName(productSearchRequest.getCategoryName()));
        }

        if (productSearchRequest.getMinPrice() != null || productSearchRequest.getMaxPrice() != null) {
            specification = specification.and(ProductSpecification.hasPriceRange(
                    Optional.ofNullable(productSearchRequest.getMinPrice()).orElse(0.0),
                    Optional.ofNullable(productSearchRequest.getMaxPrice()).orElse(Double.MAX_VALUE)
            ));
        }

        List<Product> products = productRepository.findAll(specification);
        List<Product> parentProducts = products.stream()
                .filter(product -> product.getParentProductId() == null)
                .toList();

        return parentProducts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllChildProduct() {
        List<Product> productList = productRepository.findAllChildProduct();
        if (productList.isEmpty()) {
            throw new IllegalArgumentException("Danh Sách Sản Phẩm không tồn tại" + productList);
        }
        return productList.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public void updateParentProduct(Long id, ProductUpdateParent request) {
        Product parentProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm cha không tồn tại hoặc đã bị xóa với ID: " + id));

        // Lưu tên cũ của sản phẩm cha để so sánh
        String oldParentName = parentProduct.getName();

        // Cập nhật thông tin cơ bản của sản phẩm cha
        if (request.getName() != null && !oldParentName.equalsIgnoreCase(request.getName()) &&
                productRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Name conflict detected: {}", request.getName());
            throw new NameNotExists("Tên sản phẩm đã tồn tại");
        }
        parentProduct.setName(request.getName());
        parentProduct.setDescription(request.getDescription());
        parentProduct.setSportType(request.getSportType());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Nhà cung cấp không tồn tại với ID: " + request.getSupplierId()));
        parentProduct.setSupplier(supplier);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại với ID: " + request.getCategoryId()));
        parentProduct.setCategory(category);

        if (!oldParentName.equals(request.getName())) {
            parentProduct.setName(request.getName());

            String newParentSku = generateUniqueSku(request.getName(), request.getCategoryId(), request.getSupplierId());
            parentProduct.setSku(newParentSku);

            log.info("Đã đổi tên sản phẩm cha từ '{}' thành '{}', SKU mới: '{}'", oldParentName, request.getName(), newParentSku);
        } else {
            parentProduct.setName(request.getName());
            parentProduct.setSku(request.getSku()); // hoặc giữ nguyên nếu không muốn chỉnh SKU thủ công
        }

        // Xử lý tags cho sản phẩm cha
        handleTags(request, parentProduct);

        // Xử lý ảnh mới (nếu có)
        if (request.getParentImages() != null && !request.getParentImages().isEmpty()) {
            // Xóa ảnh cũ
            productImageRepository.deleteByProductId(parentProduct.getId());
            parentProduct.getImages().clear();

            // Upload và lưu ảnh mới
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
                log.info(" Đã cập nhật {} ảnh cho sản phẩm cha {}: {}", parentProductImages.size(), parentProduct.getSku(), parentImageUrls);
            }
        }

        // Lưu sản phẩm cha trước để đảm bảo ID tồn tại
        productRepository.save(parentProduct);

        // Cập nhật các sản phẩm con
        List<Product> childProducts = productRepository.findByParentProductId(parentProduct.getId());
        for (Product childProduct : childProducts) {
            // Cập nhật các trường không phụ thuộc vào tên
            childProduct.setCategory(parentProduct.getCategory());
            childProduct.setSupplier(parentProduct.getSupplier());
            childProduct.setSportType(parentProduct.getSportType());

            // Xử lý tags cho sản phẩm con (đồng bộ với sản phẩm cha)
            handleTags(request, childProduct);

            // Cập nhật tên và SKU của sản phẩm con nếu tên sản phẩm cha thay đổi
            if (!oldParentName.equals(request.getName())) {
                // Lấy phần thông tin biến thể từ tên cũ (phần sau dấu " - ")
                String childName = childProduct.getName();
                String variantPart = childName.contains(" - ") ? childName.substring(childName.indexOf(" - ")) : "";

                // Tạo tên mới cho sản phẩm con: {tên sản phẩm cha mới} + {phần thông tin biến thể}
                String newChildName = request.getName() + variantPart;
                childProduct.setName(newChildName);

                // Tạo SKU mới cho sản phẩm con dựa trên tên mới
                String newSku = generateUniqueSku(newChildName, request.getCategoryId(), request.getSupplierId());
                childProduct.setSku(newSku);

                log.info(" Đã cập nhật sản phẩm con: Tên từ '{}' thành '{}', SKU thành '{}'", childName, newChildName, newSku);
            }

            log.info(" Đã đồng bộ sản phẩm con {}: categoryId={}, supplierId={}, sportType={}",
                    childProduct.getSku(), childProduct.getCategory().getId(), childProduct.getSupplier().getId(), childProduct.getSportType());
        }

        // Lưu tất cả sản phẩm con đã cập nhật
        if (!childProducts.isEmpty()) {
            productRepository.saveAll(childProducts);
        }

        log.info("Đã cập nhật sản phẩm cha {}", parentProduct.getSku());
    }

    private void handleTags(ProductUpdateParent request, Product product) {
        if (request.getTagId() == null || request.getTagId().isEmpty()) {
            return;
        }

        productTagMappingRepository.deleteByProductId(product.getId());
        List<ProductTagMapping> mappings = request.getTagId().stream()
                .map(tagId -> {
                    ProductTagMapping mapping = new ProductTagMapping();
                    mapping.setProduct(product);
                    mapping.setTag(productTagRepository.findById(tagId)
                            .orElseThrow(() -> new IllegalArgumentException("Tag không tồn tại với ID: " + tagId)));
                    return mapping;
                })
                .collect(Collectors.toList());
        productTagMappingRepository.saveAll(mappings);
    }


    @Override
    @Transactional
    public void updateChildProduct(Long id, ProductUpdateChild request) {
        Product childProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm con không tồn tại hoặc đã bị xóa với ID: " + id));
        // Cập nhật mô tả nếu có
        List<Product> products = productRepository.findByParentProductId(childProduct.getParentProductId());
        if(products == null || products.isEmpty()){
            throw new IllegalStateException("Không tìm thấy sản phẩm con nào thuộc parent với ID: " + childProduct.getParentProductId());
        }


        if (request.getDescription() != null) {
            childProduct.setDescription(request.getDescription());
        }

        // Cập nhật giá và số lượng tồn kho
        if (request.getPrice() != null) {
            childProduct.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            childProduct.setStockQuantity(request.getStockQuantity());
        }

        checkDuplicateAtb(request, products, childProduct.getId());
        // Cập nhật giá trị thuộc tính sản phẩm và valid thuộc tính sản phẩm bị trùng
        if (request.getProductAttributeValues() != null && !request.getProductAttributeValues().isEmpty()) {
            // Xóa thuộc tính cũ
            productAttributeValueRepository.deleteByProductId(childProduct.getId());
            childProduct.getProductAttributeValues().clear();

            // Thêm thuộc tính mới
            List<ProductAttributeValue> newAttributeValues = request.getProductAttributeValues().stream()
                    .map(attr -> {
                        ProductAttribute attribute = productAttributeRepository.findById(attr.getAttributeId())
                                .orElseThrow(() -> new IllegalArgumentException("Thuộc tính không tồn tại với ID: " + attr.getAttributeId()));
                        ProductAttributeValue value = new ProductAttributeValue();
                        value.setProduct(childProduct);
                        value.setAttribute(attribute);
                        value.setValue(attr.getValue());
                        return value;
                    })
                    .collect(Collectors.toList());

            productAttributeValueRepository.saveAll(newAttributeValues);
            childProduct.getProductAttributeValues().addAll(newAttributeValues);
            log.info("Đã cập nhật {} giá trị thuộc tính cho biến thể {}", newAttributeValues.size(), childProduct.getSku());
        }

        // Cập nhật lại tên sản phẩm con sau khi cập nhật thuộc tính
        updateChildProductName(childProduct);

        // Cập nhật ảnh sản phẩm nếu có
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Xóa ảnh cũ
            productImageRepository.deleteByProductId(childProduct.getId());
            childProduct.getImages().clear();

            // Upload ảnh mới
            List<String> imageUrls = uploadImages(request.getImages());
            if (!imageUrls.isEmpty()) {
                List<ProductImage> childProductImages = imageUrls.stream()
                        .map(url -> {
                            ProductImage productImage = new ProductImage();
                            productImage.setImageUrl(url);
                            productImage.setProduct(childProduct);
                            return productImage;
                        })
                        .collect(Collectors.toList());

                productImageRepository.saveAll(childProductImages);
                childProduct.getImages().addAll(childProductImages);
                log.info("Đã cập nhật {} ảnh cho biến thể {}: {}", childProductImages.size(), childProduct.getSku(), imageUrls);
            }
        }

        // Lưu lại sản phẩm đã cập nhật
        productRepository.save(childProduct);
        log.info("Đã cập nhật biến thể {}", childProduct.getSku());
    }

    private void checkDuplicateAtb(ProductUpdateChild request, List<Product> products, Long currentProductId) {

        // Tạo tổ hợp mới từ request, sắp xếp theo attributeId
        List<String> newCombination = request.getProductAttributeValues() != null ? request.getProductAttributeValues().stream()
                .sorted(Comparator.comparing(ProductUpdateChild.ProductAttributeValue::getAttributeId))
                .map(attr -> attr.getValue() != null ? attr.getValue().trim() : "")
                .collect(Collectors.toList()) : Collections.emptyList();

        // Kiểm tra trùng lặp với các sản phẩm con khác
        boolean isDuplicate = products.stream()
                .filter(product -> !product.getId().equals(currentProductId))
                .anyMatch(product -> {
                    List<String> existingCombination = product.getProductAttributeValues().stream()
                            .sorted(Comparator.comparing(pav -> pav.getAttribute().getId()))
                            .map(ProductAttributeValue::getValue)
                            .collect(Collectors.toList());

                    // So sánh số lượng và nội dung tổ hợp
                    return existingCombination.size() == newCombination.size() && existingCombination.equals(newCombination);
                });

        if (isDuplicate) {
            throw new AttributeValueDuplicate(" Tổ hợp giá trị thuộc tính [" + String.join(", ", newCombination) + "] đã tồn tại ở biến thể khác.");
        }

        log.info("Kiểm tra trùng lặp thành công cho tổ hợp: {}", newCombination);
    }


    @Override
    public void deleteSoft(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new ErrorException("product is not found"));
        product.setDeleted(true);
        productRepository.save(product);

    }

    @Override
    public List<ProductResponse> finByNameProductChild(String name) {
        List<Product> productList=productRepository.finByNameProductChild(name);
        List<Product> products=new ArrayList<>();
        for (Product product:productList) {
            if(product.getParentProductId()!=null){
                products.add(product);
            }
        }
        return products.stream().map(product -> mapToResponse(product)).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> finChildProByCateId(Long id) {
        List<Product> productList=productRepository.findChildProductsByCategoryId(id);
        return productList.stream().map(product -> mapToResponse(product)).collect(Collectors.toList());
    }


    public List<ProductAttributeValue> getProductAttributeValuesByProductId(Long productId) {
        return productAttributeValueRepository.findByProductParentProductId(productId);
    }

    public List<String> getAttributeNamesByIds(List<Long> attributeIds) {
        return productAttributeRepository.findAllById(attributeIds).stream()
                .map(ProductAttribute::getName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<VariantCountDTO> getVariantCounts() {
        return productRepository.quantityVariants();
    }


    @Override
    @Transactional
    public void validateAttributesAndValues(Long productId, List<AddProductChild.ProductAttributeValue> newAttributes) {
        // 1. Kiểm tra đầu vào cơ bản
        if (newAttributes == null || newAttributes.isEmpty()) {
            throw new IllegalArgumentException("Danh sách thuộc tính mới không được để trống.");
        }

        // 2. Lấy danh sách tất cả các biến thể hiện có của sản phẩm cha
        List<ProductAttributeValue> existingAttributes = getProductAttributeValuesByProductId(productId);
        if (existingAttributes.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy biến thể nào cho sản phẩm cha với ID: " + productId);
        }

        // 3. Lấy product_id của biến thể con đầu tiên
        Long firstVariantProductId = existingAttributes.stream()
                .map(ProductAttributeValue::getProduct)
                .map(Product::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy product_id đầu tiên cho sản phẩm cha với ID: " + productId));

        // 4. Lấy tất cả thuộc tính của biến thể con đầu tiên
        List<ProductAttributeValue> firstVariantAttributes = existingAttributes.stream()
                .filter(pav -> pav.getProduct().getId().equals(firstVariantProductId))
                .sorted(Comparator.comparing(pav -> pav.getAttribute().getId()))
                .collect(Collectors.toList());

        if (firstVariantAttributes.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy biến thể con đầu tiên để tham chiếu cho sản phẩm cha với ID: " + productId);
        }

        // 5. Lấy tập hợp attribute_id từ biến thể con đầu tiên
        Set<Long> firstVariantAttributeIds = firstVariantAttributes.stream()
                .map(pav -> pav.getAttribute().getId())
                .collect(Collectors.toSet());

        // 6. Lấy tập hợp attribute_id từ sản phẩm con mới
        Set<Long> newAttributeIds = newAttributes.stream()
                .map(AddProductChild.ProductAttributeValue::getAttributeId)
                .collect(Collectors.toSet());

        // 7. Kiểm tra tập hợp attribute_id phải khớp với biến thể con đầu tiên
        if (!newAttributeIds.equals(firstVariantAttributeIds)) {
            List<Long> invalidAttributeIds = new ArrayList<>(newAttributeIds);
            invalidAttributeIds.removeAll(firstVariantAttributeIds);
            List<String> invalidAttributeNames = getAttributeNamesByIds(invalidAttributeIds);
            String errorMessage = "Tập hợp thuộc tính không khớp với biến thể con đầu tiên. Thuộc tính không hợp lệ: " +
                    (invalidAttributeNames.isEmpty() ? "Số lượng hoặc loại thuộc tính không khớp" : String.join(", ", invalidAttributeNames));
            log.error(errorMessage + " | newAttributeIds: {} | firstVariantAttributeIds: {}", newAttributeIds, firstVariantAttributeIds);
            throw new IllegalArgumentException(errorMessage);
        }

        // 8. Kiểm tra tổ hợp giá trị (value) không được trùng với bất kỳ biến thể nào hiện có
        Map<Long, List<ProductAttributeValue>> groupedByProduct = existingAttributes.stream()
                .collect(Collectors.groupingBy(pav -> pav.getProduct().getId()));
        List<List<String>> existingValueCombinations = groupedByProduct.values().stream()
                .map(list -> list.stream()
                        .sorted(Comparator.comparing(pav -> pav.getAttribute().getId()))
                        .map(pav -> pav.getValue() != null ? pav.getValue().trim().toLowerCase().replaceAll("\\s+", " ") : "")
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        List<String> newValues = newAttributes.stream()
                .sorted(Comparator.comparing(AddProductChild.ProductAttributeValue::getAttributeId))
                .map(attr -> attr.getValue() != null ? attr.getValue().trim().toLowerCase().replaceAll("\\s+", " ") : "")
                .collect(Collectors.toList());

        // 9. Kiểm tra giá trị không được để trống
        if (newValues.stream().anyMatch(String::isEmpty)) {
            throw new IllegalArgumentException("Giá trị thuộc tính không được để trống.");
        }

        // 10. Kiểm tra tổ hợp giá trị đã tồn tại
        if (existingValueCombinations.contains(newValues)) {
            String errorMessage = "Tổ hợp giá trị thuộc tính đã tồn tại: " + String.join(", ", newValues);
            log.error(errorMessage + " | newValues: {} | existingValueCombinations: {}", newValues, existingValueCombinations);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private List<String> uploadImagesVariants(AddProductChild.ProductVariant variant) {
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

    private List<ProductAttributeValue> mapAttributesToValueVariants(Product product, List<String> combination, List<AddProductChild.ProductAttributeValue> productAttributeValues) {
        List<ProductAttributeValue> values = new ArrayList<>();

        // Lấy danh sách attributeId theo thứ tự từ productAttributeValues
        List<Long> attributeIds = productAttributeValues.stream()
                .map(AddProductChild.ProductAttributeValue::getAttributeId)
                .distinct()
                .collect(Collectors.toList());

        // Nhóm các giá trị theo attributeId
        Map<Long, Set<String>> attributeValueMap = new LinkedHashMap<>();
        for (AddProductChild.ProductAttributeValue pav : productAttributeValues) {
            attributeValueMap.computeIfAbsent(pav.getAttributeId(), k -> new HashSet<>()).add(pav.getValue());
        }

        // Kiểm tra xem combination có đủ giá trị không
        if (combination.size() != attributeIds.size()) {
            throw new IllegalArgumentException("Số giá trị trong tổ hợp không khớp với số thuộc tính!");
        }

        // Duyệt qua attributeId theo thứ tự
        for (int i = 0; i < attributeIds.size(); i++) {
            Long attributeId = attributeIds.get(i);
            String value = combination.get(i); // Lấy giá trị từ combination theo đúng vị trí

            // Kiểm tra giá trị hợp lệ
            Set<String> validValues = attributeValueMap.get(attributeId);
            if (!validValues.contains(value)) {
                throw new IllegalArgumentException("Giá trị '" + value + "' không hợp lệ cho attributeId: " + attributeId);
            }

            // Tạo ProductAttributeValue
            ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new IllegalArgumentException("Thuộc tính không tồn tại: " + attributeId));
            ProductAttributeValue pav = new ProductAttributeValue();
            pav.setProduct(product);
            pav.setAttribute(attribute);
            pav.setValue(value);
            values.add(pav);
        }

        return values;
    }
    @Override
    public void addVariantsToExistingProduct(AddProductChild child) {
        Product parentProduct = productRepository.findById(child.getParentProductId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm cha không tồn tại với ID: " + child.getParentProductId()));
        // hiển thị lên sản phẩm cha

        List<Set<String>> attributeValues = child.getProductAttributeValues().stream()
                .collect(Collectors.groupingBy(
                        AddProductChild.ProductAttributeValue::getAttributeId,
                        Collectors.mapping(AddProductChild.ProductAttributeValue::getValue, Collectors.toSet())
                )).values().stream().collect(Collectors.toList());

        List<List<String>> valueCombinations = generateCombinations(attributeValues);
        int expectedCombinations = attributeValues.stream().mapToInt(Set::size).reduce(1, (a, b) -> a * b);
        if (valueCombinations.size() != expectedCombinations) {
            throw new IllegalArgumentException("Số tổ hợp không khớp với số giá trị thuộc tính!");
        }

        if (child.getVariants().size() != valueCombinations.size()) {
            throw new IllegalArgumentException("Số biến thể (" + child.getVariants().size() + ") không khớp với số tổ hợp (" + valueCombinations.size() + ")!");
        }

        // Kiểm tra số lượng ảnh (nếu có)
//        if (child.getVariantImages() != null && !request.getVariantImages().isEmpty() && request.getVariantImages().size() < valueCombinations.size()) {
//            throw new IllegalArgumentException("Số ảnh (" + request.getVariantImages().size() + ") không đủ cho " + valueCombinations.size() + " tổ hợp!");
//        }

        List<Product> childProducts = new ArrayList<>();
        List<ProductAttributeValue> attributeValuesList = new ArrayList<>();
        AtomicInteger variantIndex = new AtomicInteger(0);

        for (List<String> combination : valueCombinations) {
            AddProductChild.ProductVariant variant = child.getVariants().get(variantIndex.get());
            Product childProduct = new Product();
            String variantName = String.join(" - ", combination);
            childProduct.setName(parentProduct.getName() + " - " + variantName);
            childProduct.setDescription(parentProduct.getDescription());
            childProduct.setSportType(parentProduct.getSportType());
            childProduct.setPrice(variant.getPrice() != null ? variant.getPrice() : 0.0);
            childProduct.setStockQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
            childProduct.setParentProductId(parentProduct.getId());
            childProduct.setSupplier(parentProduct.getSupplier());
            childProduct.setCategory(parentProduct.getCategory());
            childProduct.setDeleted(false);
            String sku = generateUniqueSku(parentProduct.getName(), parentProduct.getCategory().getId(), parentProduct.getSupplier().getId());
            childProduct.setSku(sku);

            // Tải ảnh và thêm vào danh sách images của childProduct
            List<String> imageUrls = uploadImagesVariants(variant);
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
                System.out.println(" Không có ảnh riêng, sử dụng ảnh cha cho biến thể " + childProduct.getSku());
            }

            childProducts.add(childProduct);
            attributeValuesList.addAll(mapAttributesToValueVariants(childProduct, combination, child.getProductAttributeValues()));
            variantIndex.incrementAndGet();
        }

        productRepository.saveAll(childProducts);
        productAttributeValueRepository.saveAll(attributeValuesList);

    }



    private void updateChildProductName(Product childProduct) {
        // Lấy sản phẩm cha
        Product parentProduct = productRepository.findById(childProduct.getParentProductId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm cha không tồn tại với ID: " + childProduct.getParentProductId()));

        // Lấy danh sách thuộc tính còn lại của sản phẩm con
        List<ProductAttributeValue> remainingAttributes = childProduct.getProductAttributeValues();

        // Xây dựng tên mới: {tên sản phẩm cha}-{thuộc tính 1}-{thuộc tính 2}-...
        StringBuilder newNameBuilder = new StringBuilder(parentProduct.getName());
        for (ProductAttributeValue attr : remainingAttributes) {
            newNameBuilder.append("-").append(attr.getValue());
        }
        String newChildName = newNameBuilder.toString();
        log.info(" Đã cập nhật tên sản phẩm con từ '{}' thành '{}'", childProduct.getName(), newChildName);
        childProduct.setName(newChildName);
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


//    @Override
//    @Transactional(rollbackOn = Exception.class)
//    public void createProductWithVariants(List<ProductRequest> requests, MultipartFile[] images) {
//        if (requests.isEmpty()) {
//            throw new IllegalArgumentException("Danh sách yêu cầu sản phẩm trống!");
//        }
//
//        ProductRequest request = requests.get(0);
//        Product parentProduct = createParentProduct(request);
//        handleTags(request, parentProduct);
//
//        // Upload và lưu ảnh cho sản phẩm cha
//        List<String> parentImageUrls = uploadImages(request.getParentImages());
//        List<ProductImage> parentProductImages = new ArrayList<>();
//        if (!parentImageUrls.isEmpty()) {
//            parentProductImages = parentImageUrls.stream()
//                    .map(url -> {
//                        ProductImage productImage = new ProductImage();
//                        productImage.setImageUrl(url);
//                        productImage.setProduct(parentProduct);
//                        return productImage;
//                    })
//                    .collect(Collectors.toList());
//            productImageRepository.saveAll(parentProductImages);
//            parentProduct.getImages().addAll(parentProductImages);
//            productRepository.save(parentProduct);
//            log.info("Đã gán {} ảnh cho sản phẩm cha {}: {}", parentProductImages.size(), parentProduct.getSku(), parentImageUrls);
//        }
//
//        // Tạo danh sách attributeValues theo đúng thứ tự của productAttributeValues
//        List<Long> attributeIds = request.getProductAttributeValues().stream()
//                .map(ProductRequest.ProductAttributeValue::getAttributeId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<Set<String>> attributeValues = attributeIds.stream()
//                .map(attrId -> request.getProductAttributeValues().stream()
//                        .filter(pav -> pav.getAttributeId().equals(attrId))
//                        .map(ProductRequest.ProductAttributeValue::getValue)
//                        .collect(Collectors.toSet()))
//                .collect(Collectors.toList());
//
//        List<List<String>> valueCombinations = generateCombinations(attributeValues);
//        int expectedCombinations = attributeValues.stream().mapToInt(Set::size).reduce(1, (a, b) -> a * b);
//        if (valueCombinations.size() != expectedCombinations) {
//            throw new IllegalArgumentException("Số tổ hợp không khớp với số giá trị thuộc tính!");
//        }
//
//        if (request.getVariants().size() != valueCombinations.size()) {
//            throw new IllegalArgumentException("Số biến thể (" + request.getVariants().size() + ") không khớp với số tổ hợp (" + valueCombinations.size() + ")!");
//        }
//        if (images != null && images.length > 0 && images.length < valueCombinations.size()) {
//            throw new IllegalArgumentException("Số ảnh (" + images.length + ") không đủ cho " + valueCombinations.size() + " tổ hợp!");
//        }
//
//        // Gán ảnh cho các biến thể nếu không có ảnh riêng
//        if (images != null && images.length > 0) {
 //           associateImagesWithVariants(request.getVariants(), images);
//        }
//
//        List<Product> childProducts = new ArrayList<>();
//        List<ProductImage> allChildProductImages = new ArrayList<>();
//        List<ProductAttributeValue> attributeValuesList = new ArrayList<>();
//        AtomicInteger variantIndex = new AtomicInteger(0);
//
//        for (List<String> combination : valueCombinations) {
//            ProductRequest.ProductVariant variant = request.getVariants().get(variantIndex.get());
//            Product childProduct = new Product();
//            String variantName = String.join(" - ", combination);
//            childProduct.setName(request.getName() + " - " + variantName);
//            childProduct.setDescription(request.getDescription());
//            childProduct.setSportType(request.getSportType());
//            childProduct.setPrice(variant.getPrice() != null ? variant.getPrice() : 0.0);
//            childProduct.setStockQuantity(variant.getStockQuantity() != null ? variant.getStockQuantity() : 0);
//            childProduct.setParentProductId(parentProduct.getId());
//            childProduct.setSupplier(parentProduct.getSupplier());
//            childProduct.setCategory(parentProduct.getCategory());
//            childProduct.setDeleted(false);
//            String baseSku = request.getSku() != null && !request.getSku().isEmpty()
//                    ? request.getSku()
//                    : generateUniqueSku(request.getName(), request.getCategoryId(), request.getSupplierId());
////            String sku = baseSku + "-" + String.join("-", combination);
//            childProduct.setSku(baseSku);
//
//            // Tải ảnh và thêm vào danh sách images của childProduct
//            List<String> imageUrls = uploadImages(variant);
//            List<ProductImage> childProductImages = new ArrayList<>();
//            for (String imageUrl : imageUrls) {
//                if (!imageUrl.isEmpty()) {
//                    ProductImage productImage = new ProductImage();
//                    productImage.setImageUrl(imageUrl);
//                    productImage.setProduct(childProduct);
//                    childProductImages.add(productImage);
//                    childProduct.getImages().add(productImage);
//                }
//            }
//
//            if (childProductImages.isEmpty()) {
//                if (parentProduct.getImages().isEmpty()) {
//                    throw new IllegalStateException("Không có ảnh nào cho sản phẩm cha hoặc sản phẩm con: " + childProduct.getSku());
//                }
//                for (ProductImage parentImage : parentProduct.getImages()) {
//                    ProductImage childImage = new ProductImage();
//                    childImage.setImageUrl(parentImage.getImageUrl());
//                    childImage.setProduct(childProduct);
//                    childProductImages.add(childImage);
//                    childProduct.getImages().add(childImage);
//                }
//                log.info("Không có ảnh riêng, sử dụng ảnh cha cho biến thể {}", childProduct.getSku());
//            }
//
//            allChildProductImages.addAll(childProductImages);
//            childProducts.add(childProduct);
//            attributeValuesList.addAll(mapAttributesToValues(childProduct, combination, request.getProductAttributeValues()));
//            variantIndex.incrementAndGet();
//        }
//
//        List<Product> allProducts = new ArrayList<>();
//        allProducts.add(parentProduct);
//        allProducts.addAll(childProducts);
//        productRepository.saveAll(allProducts);
//
//        List<ProductImage> allImages = new ArrayList<>();
//        allImages.addAll(parentProductImages);
//        allImages.addAll(allChildProductImages);
//        productImageRepository.saveAll(allImages);
//
//        productAttributeValueRepository.saveAll(attributeValuesList);
//
//        for (Product child : childProducts) {
//            handleTags(request, child);
//        }
//    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createProductWithVariants(List<ProductRequest> requests, MultipartFile[] imagesFromController) {
        if (requests.isEmpty()) {
            throw new IllegalArgumentException("Danh sách yêu cầu sản phẩm trống!");
        }

        ProductRequest request = requests.get(0);
        Product parentProduct = createParentProduct(request);
        handleTags(request, parentProduct);

        // Upload và lưu ảnh cho sản phẩm cha
        List<String> parentImageUrls = uploadImages(request.getParentImages());
        List<ProductImage> parentProductImages = new ArrayList<>();
        if (!parentImageUrls.isEmpty()) {
            parentProductImages = parentImageUrls.stream()
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
            log.info("Đã gán {} ảnh cho sản phẩm cha {}: {}", parentProductImages.size(), parentProduct.getSku(), parentImageUrls);
        }

        // Tạo danh sách attributeValues theo đúng thứ tự của productAttributeValues
        // Lấy danh sách các attributeId duy nhất theo thứ tự xuất hiện trong JSON
        List<Long> attributeIds = request.getProductAttributeValues().stream()
                .map(ProductRequest.ProductAttributeValue::getAttributeId)
                .distinct()
                .collect(Collectors.toList());

        // --- PHẦN ĐÃ SỬA ĐỔI ĐỂ DUY TRÌ THỨ TỰ CỦA CÁC GIÁ TRỊ THUỘC TÍNH ---
        List<Set<String>> attributeValues = attributeIds.stream()
                .map(attrId -> request.getProductAttributeValues().stream()
                        .filter(pav -> pav.getAttributeId().equals(attrId))
                        .map(ProductRequest.ProductAttributeValue::getValue)
                        // THAY ĐỔI TỪ Collectors.toSet() sang Collectors.toCollection(LinkedHashSet::new)
                        .collect(Collectors.toCollection(LinkedHashSet::new))) // <-- Đảm bảo thứ tự chèn được duy trì
                .collect(Collectors.toList());
        // --- KẾT THÚC PHẦN ĐÃ SỬA ĐỔI ---

        List<List<String>> valueCombinations = generateCombinations(attributeValues);
        int expectedCombinations = attributeValues.stream().mapToInt(Set::size).reduce(1, (a, b) -> a * b);

        if (valueCombinations.size() != expectedCombinations) {
            throw new IllegalArgumentException("Số tổ hợp không khớp với số giá trị thuộc tính!");
        }

        if (request.getVariants().size() != valueCombinations.size()) {
            throw new IllegalArgumentException("Số biến thể (" + request.getVariants().size() + ") không khớp với số tổ hợp (" + valueCombinations.size() + ")!");
        }

        List<Product> childProducts = new ArrayList<>();
        List<ProductImage> allChildProductImages = new ArrayList<>();
        List<ProductAttributeValue> attributeValuesList = new ArrayList<>();

        for (int i = 0; i < valueCombinations.size(); i++) {
            List<String> combination = valueCombinations.get(i);
            ProductRequest.ProductVariant variant = request.getVariants().get(i);

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
            childProduct.setDeleted(false);
            String baseSku = request.getSku() != null && !request.getSku().isEmpty()
                    ? request.getSku()
                    : generateUniqueSku(request.getName(), request.getCategoryId(), request.getSupplierId());
            childProduct.setSku(baseSku);

            List<String> imageUrls = new ArrayList<>();
            if (variant.getImages() != null && !variant.getImages().isEmpty()) {
                imageUrls.addAll(uploadImages(variant.getImages()));
                log.info("Đã gán {} ảnh riêng cho biến thể {}: {}", variant.getImages().size(), childProduct.getName(), imageUrls);
            }

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

            if (childProductImages.isEmpty()) {
                if (parentProduct.getImages().isEmpty()) {
                    throw new IllegalStateException("Không có ảnh nào cho sản phẩm cha hoặc sản phẩm con: " + childProduct.getSku());
                }
                for (ProductImage parentImage : parentProduct.getImages()) {
                    ProductImage childImage = new ProductImage();
                    childImage.setImageUrl(parentImage.getImageUrl());
                    childImage.setProduct(childProduct);
                    childProductImages.add(childImage);
                    childProduct.getImages().add(childImage);
                }
                log.info("Không có ảnh riêng, sử dụng ảnh cha cho biến thể {}", childProduct.getSku());
            }

            allChildProductImages.addAll(childProductImages);
            childProducts.add(childProduct);
            attributeValuesList.addAll(mapAttributesToValues(childProduct, combination, request.getProductAttributeValues()));
        }

        List<Product> allProducts = new ArrayList<>();
        allProducts.add(parentProduct);
        allProducts.addAll(childProducts);
        productRepository.saveAll(allProducts);

        List<ProductImage> allImages = new ArrayList<>();
        allImages.addAll(parentProductImages);
        allImages.addAll(allChildProductImages);
        productImageRepository.saveAll(allImages);

        productAttributeValueRepository.saveAll(attributeValuesList);

        for (Product child : childProducts) {
            handleTags(request, child);
        }
    }
    private List<ProductAttributeValue> mapAttributesToValues(Product product, List<String> combination, List<ProductRequest.ProductAttributeValue> productAttributeValues) {
        List<ProductAttributeValue> values = new ArrayList<>();

        List<Long> attributeIds = productAttributeValues.stream()
                .map(ProductRequest.ProductAttributeValue::getAttributeId)
                .distinct()
                .collect(Collectors.toList());

        // THAY ĐỔI TỪ HashMap/HashSet sang LinkedHashMap/LinkedHashSet để duy trì thứ tự
        Map<Long, Set<String>> attributeValueMap = new LinkedHashMap<>();
        for (ProductRequest.ProductAttributeValue pav : productAttributeValues) {
            attributeValueMap.computeIfAbsent(pav.getAttributeId(), k -> new LinkedHashSet<>()).add(pav.getValue());
        }

        if (combination.size() != attributeIds.size()) {
            throw new IllegalArgumentException("Số giá trị trong tổ hợp không khớp với số thuộc tính!");
        }

        for (int i = 0; i < attributeIds.size(); i++) {
            Long attributeId = attributeIds.get(i);
            String value = combination.get(i);

            Set<String> validValues = attributeValueMap.get(attributeId);
            if (!validValues.contains(value)) {
                throw new IllegalArgumentException("Giá trị '" + value + "' không hợp lệ cho attributeId: " + attributeId);
            }

            ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new IllegalArgumentException("Thuộc tính không tồn tại: " + attributeId));
            ProductAttributeValue pav = new ProductAttributeValue();
            pav.setProduct(product);
            pav.setAttribute(attribute);
            pav.setValue(value);
            values.add(pav);
        }

        return values;
    }

//    private List<ProductAttributeValue> mapAttributesToValues(Product product, List<String> combination, List<ProductRequest.ProductAttributeValue> productAttributeValues) {
//        List<ProductAttributeValue> values = new ArrayList<>();
//
//        // Lấy danh sách attributeId theo thứ tự từ productAttributeValues
//        List<Long> attributeIds = productAttributeValues.stream()
//                .map(ProductRequest.ProductAttributeValue::getAttributeId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        // Nhóm các giá trị theo attributeId
//        Map<Long, Set<String>> attributeValueMap = new LinkedHashMap<>();
//        for (ProductRequest.ProductAttributeValue pav : productAttributeValues) {
//            attributeValueMap.computeIfAbsent(pav.getAttributeId(), k -> new HashSet<>()).add(pav.getValue());
//        }
//
//        // Kiểm tra xem combination có đủ giá trị không
//        if (combination.size() != attributeIds.size()) {
//            throw new IllegalArgumentException("Số giá trị trong tổ hợp không khớp với số thuộc tính!");
//        }
//
//        // Duyệt qua attributeId theo thứ tự
//        for (int i = 0; i < attributeIds.size(); i++) {
//            Long attributeId = attributeIds.get(i);
//            String value = combination.get(i); // Lấy giá trị từ combination theo đúng vị trí
//
//            // Kiểm tra giá trị hợp lệ
//            Set<String> validValues = attributeValueMap.get(attributeId);
//            if (!validValues.contains(value)) {
//                throw new IllegalArgumentException("Giá trị '" + value + "' không hợp lệ cho attributeId: " + attributeId);
//            }
//
//            // Tạo ProductAttributeValue
//            ProductAttribute attribute = productAttributeRepository.findById(attributeId)
//                    .orElseThrow(() -> new IllegalArgumentException("Thuộc tính không tồn tại: " + attributeId));
//            ProductAttributeValue pav = new ProductAttributeValue();
//            pav.setProduct(product);
//            pav.setAttribute(attribute);
//            pav.setValue(value);
//            values.add(pav);
//        }
//
//        return values;
//    }


    private Product createParentProduct(ProductRequest request) {
        Product parentProduct = new Product();
        if(productRepository.existsByNameIgnoreCase(request.getName())){
            throw new NameNotExists("sản phẩm đã tồn tại");
        }
        parentProduct.setName(request.getName());
        parentProduct.setDescription(request.getDescription());
        parentProduct.setSportType(request.getSportType());
        parentProduct.setPrice(0.0);
        parentProduct.setStockQuantity(0);
        parentProduct.setDeleted(false);
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ErrorException("Nhà cung cấp không tồn tại"));
        parentProduct.setSupplier(supplier);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ErrorException("Danh mục không tồn tại"));
        parentProduct.setCategory(category);

        String sku = request.getSku() != null && !request.getSku().isEmpty() ? request.getSku() : generateUniqueSku(request.getName(), request.getCategoryId(), request.getSupplierId());
        parentProduct.setSku(sku);
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


    // xử lý ảnh sản phẩm của cha
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
                            ProductResponse.ProductAttributeValueResponse optionResponse = new ProductResponse.ProductAttributeValueResponse();
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

}
