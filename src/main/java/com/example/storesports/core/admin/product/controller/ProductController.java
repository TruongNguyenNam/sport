package com.example.storesports.core.admin.product.controller;

import com.example.storesports.core.admin.product.payload.*;
import com.example.storesports.entity.Product;
import com.example.storesports.infrastructure.exceptions.AttributeValueDuplicate;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.exceptions.NameNotExists;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.product.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping(value = "api/v1/admin/product")
@Validated
@RequiredArgsConstructor
@Tag(name = "Product", description = "Endpoints for managing products")
@Slf4j
public class ProductController {
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @GetMapping("/parent")
    public ResponseData<List<ProductResponse>> getAllParentProducts() {
        List<ProductResponse> products = productService.getAllParentProduct();
        return ResponseData.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm cha thành công")
                .data(products)
                .build();
    }

    @GetMapping("/variant-counts")
    public ResponseData<List<VariantCountDTO>> getVariantCounts() {
        List<VariantCountDTO> variantCounts = productService.getVariantCounts();
        return ResponseData.<List<VariantCountDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách số lượng biến thể thành công")
                .data(variantCounts)
                .build();
    }

    @PostMapping(value = "/{parentProductId}/variants", consumes = {"multipart/form-data"})
    public ResponseData<Void> addVariantsToProduct(
            @PathVariable Long parentProductId,
            @RequestParam("request") String requestJson,
            @RequestParam(value = "variantImages", required = false) MultipartFile[] variantImages) {
        log.info("Received JSON data: {}", requestJson);
        log.info("Received {} variant images", (variantImages != null ? variantImages.length : 0));

        try {
            AddProductChild request = objectMapper.readValue(
                    requestJson,
                    AddProductChild.class
            );
            request.setParentProductId(parentProductId);
            log.info("Deserialized request: {}", request);
            productService.validateAttributesAndValues(parentProductId, request.getProductAttributeValues());

            if (variantImages != null && variantImages.length > 0) {
                int index = 0;
                for (AddProductChild.ProductVariant variant : request.getVariants()) {
                    if (index < variantImages.length) {
                        variant.setImages(new ArrayList<>(List.of(variantImages[index])));
                        index++;
                    } else {
                        variant.setImages(new ArrayList<>());
                    }
                }
            } else {
                for (AddProductChild.ProductVariant variant : request.getVariants()) {
                    variant.setImages(new ArrayList<>());
                }
            }

            productService.addVariantsToExistingProduct(request);
            return ResponseData.<Void>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("Thêm biến thể sản phẩm thành công")
                    .build();
        } catch (ErrorException e) {
            log.error("Error parsing AddProductChild request", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu JSON không hợp lệ");
        } catch (IllegalArgumentException e) {
            log.warn("Validation error: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while adding variants", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
        }
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseData<Void> addProduct(
            @RequestParam("products") String productsJson,
            @RequestParam(value = "parentImages", required = false) MultipartFile[] parentImages,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        log.info("Received JSON data: {}", productsJson);
        log.info("Received {} parent images", (parentImages != null ? parentImages.length : 0));
        log.info("Received {} variant images", (images != null ? images.length : 0));

        try {
            List<ProductRequest> requests = objectMapper.readValue(
                    productsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ProductRequest.class)
            );

            if (requests.isEmpty()) {
                throw new IllegalArgumentException("Danh sách yêu cầu sản phẩm trống!");
            }

            ProductRequest request = requests.get(0);
            List<ProductRequest.ProductVariant> variants = request.getVariants();

            // Gán ảnh cha
            if (parentImages != null && parentImages.length > 0) {
                request.setParentImages(Arrays.asList(parentImages));
            } else {
                request.setParentImages(new ArrayList<>());
            }

            // --- PHẦN QUAN TRỌNG CẦN ĐẢM BẢO TỪ FRONTEND ---
            // Gán ảnh biến thể
            if (images != null && images.length > 0) {
                if (images.length != variants.size()) {
                    // Thêm thông báo rõ ràng hơn nếu cần thiết
                    throw new IllegalArgumentException("Số ảnh biến thể (" + images.length + ") không khớp với số biến thể (" + variants.size() + "). Vui lòng đảm bảo thứ tự ảnh và biến thể khớp nhau.");
                }
                // Gán từng ảnh vào biến thể tương ứng theo chỉ mục
                for (int i = 0; i < variants.size(); i++) {
                    variants.get(i).setImages(List.of(images[i]));
                    // Log để kiểm tra
                    log.info("Gán ảnh '{}' cho biến thể thứ {}.", images[i].getOriginalFilename(), i);
                }
            } else {
                // Nếu không có ảnh biến thể nào được gửi, đảm bảo danh sách ảnh của biến thể là trống
                for (ProductRequest.ProductVariant variant : variants) {
                    variant.setImages(new ArrayList<>());
                }
            }

            productService.createProductWithVariants(requests, images); // Truyền mảng 'images' gốc nếu service cần

            return ResponseData.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Thêm sản phẩm thành công")
                    .build();

        } catch (NameNotExists e) {
            log.warn("Tên sản phẩm đã tồn tại: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi tham số: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
        }
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<ResponseData<Void>> addProductV1(
            @RequestParam("products") String productsJson,
            @RequestParam(value = "parentImages", required = false) MultipartFile[] parentImages,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            // Chuyển đổi JSON string thành List<ProductRequest>
            List<ProductRequest> requests = objectMapper.readValue(
                    productsJson,
                    new TypeReference<List<ProductRequest>>() {}
            );

            // Validate từng ProductRequest
            for (ProductRequest request : requests) {
                validateProductRequest(request);

                // Gán parentImages vào ProductRequest (nếu cần dùng cho tất cả sản phẩm)
                request.setParentImages(parentImages != null ? List.of(parentImages) : Collections.emptyList());

                // Gán ảnh cho các biến thể
                if (images != null && images.length > 0) {
                    List<ProductRequest.ProductVariant> variants = request.getVariants();
                    if (variants != null && !variants.isEmpty()) {
                        int imagesPerVariant = Math.max(1, images.length / variants.size());
                        for (int i = 0; i < variants.size(); i++) {
                            int startIdx = i * imagesPerVariant;
                            int endIdx = Math.min(startIdx + imagesPerVariant, images.length);
                            List<MultipartFile> variantImages = List.of(Arrays.copyOfRange(images, startIdx, endIdx));
                            variants.get(i).setImages(variantImages);
                        }
                    }
                }
            }

            // Gọi service xử lý tạo sản phẩm và biến thể
            productService.createProductWithVariantsV1(requests, parentImages);

            ResponseData<Void> response = new ResponseData<>(HttpStatus.CREATED.value(),
                    "Tạo sản phẩm và biến thể thành công!", null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseData<>(HttpStatus.BAD_REQUEST.value(), "Lỗi: " + e.getMessage(), null));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi hệ thống: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseData<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi không xác định: " + e.getMessage(), null));
        }
    }

    private void validateProductRequest(ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống!");
        }
        if (request.getSupplierId() == null) {
            throw new IllegalArgumentException("Nhà sản xuất không được null!");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Danh mục không được null!");
        }
        if (request.getProductAttributeValues() == null || request.getProductAttributeValues().isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một thuộc tính!");
        }
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new IllegalArgumentException("Cần ít nhất một biến thể sản phẩm!");
        }
        for (ProductRequest.ProductVariant variant : request.getVariants()) {
            if (variant.getPrice() == null || variant.getPrice() <= 0) {
                throw new IllegalArgumentException("Giá phải lớn hơn 0!");
            }
            if (variant.getStockQuantity() == null || variant.getStockQuantity() < 0) {
                throw new IllegalArgumentException("Số lượng tồn kho phải >= 0!");
            }
        }
    }


//    @PostMapping(consumes = {"multipart/form-data"})
//    public ResponseData<Void> addProduct(
//            @RequestParam("products") String productsJson,
//            @RequestParam(value = "parentImages", required = false) MultipartFile[] parentImages,
//            @RequestParam(value = "images", required = false) MultipartFile[] images) {
//
//        log.info("Received JSON data: {}", productsJson);
//        log.info("Received {} parent images", (parentImages != null ? parentImages.length : 0));
//        log.info("Received {} images", (images != null ? images.length : 0));
//
//        try {
//            List<ProductRequest> requests = objectMapper.readValue(
//                    productsJson,
//                    objectMapper.getTypeFactory().constructCollectionType(List.class, ProductRequest.class)
//            );
//
//            if (requests.isEmpty()) {
//                throw new IllegalArgumentException("Danh sách yêu cầu sản phẩm trống!");
//            }
//            for (int i = 0; i < requests.size(); i++) {
//                ProductRequest req = requests.get(i);
//                Set<ConstraintViolation<ProductRequest>> violations = validator.validate(req);
//
//                if (!violations.isEmpty()) {
//                    Map<String, String> errors = new HashMap<>();
//                    for (ConstraintViolation<ProductRequest> violation : violations) {
//                        String path = "products[" + i + "]." + violation.getPropertyPath();
//                        errors.put(path, violation.getMessage());
//                    }
//                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
//                }
//            }
//
//            ProductRequest request = requests.get(0);
//            if (parentImages != null && parentImages.length > 0) {
//                request.setParentImages(new ArrayList<>(Arrays.asList(parentImages)));
//            } else {
//                request.setParentImages(new ArrayList<>());
//            }
//
//            if (images != null && images.length > 0) {
//                int index = 0;
//                for (ProductRequest.ProductVariant variant : request.getVariants()) {
//                    if (index < images.length) {
//                        variant.setImages(new ArrayList<>(List.of(images[index])));
//                        index++;
//                    } else {
//                        variant.setImages(new ArrayList<>());
//                    }
//                }
//            } else {
//                for (ProductRequest.ProductVariant variant : request.getVariants()) {
//                    variant.setImages(new ArrayList<>());
//                }
//            }
//            productService.createProductWithVariants(requests, images);
//            return ResponseData.<Void>builder()
//                    .status(HttpStatus.OK.value())
//                    .message("Thêm sản phẩm thành công")
//                    .build();
//        } catch (NameNotExists e) {
//            log.warn("Lỗi nghiệp vụ - Tên sản phẩm đã tồn tại: {}", e.getMessage());
//            throw e; // Ném lại để GlobalException xử lý
//        } catch (IllegalArgumentException e) {
//            log.warn("Lỗi tham số không hợp lệ: {}", e.getMessage());
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
//        } catch (Exception e) {
//            log.error("Unexpected error while creating product: {}", e.getMessage(), e);
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
//        }
//    }


    @GetMapping("/child")
    public ResponseData<List<ProductResponse>> getAllChildProducts(){
        List<ProductResponse> products = productService.getAllChildProduct();
        return ResponseData.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách sản phẩm con thành công")
                .data(products)
                .build();
    }

    @PutMapping("/parent/{id}")
    public ResponseData<Void> updateParentProduct(
            @PathVariable Long id,
            @RequestParam("product") String productJson,
            @RequestParam(value = "parentImages", required = false) MultipartFile[] parentImages) {

        try {
            ProductUpdateParent request = objectMapper.readValue(productJson, ProductUpdateParent.class);

            if (parentImages != null) {
                request.setParentImages(List.of(parentImages));
            }

            productService.updateParentProduct(id, request);

            return ResponseData.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cập nhật sản phẩm cha thành công")
                    .build();

        }
        catch (NameNotExists e) {
            throw e;
        }
        catch (Exception e) {
            log.error("Unexpected error while updating parent product", e);
            throw new RuntimeException("Lỗi hệ thống, vui lòng thử lại sau", e);
        }
    }



    @PutMapping("/child/{id}")
    public ResponseEntity<ResponseData<?>> updateChildProduct(
            @PathVariable Long id,
            @RequestParam("product") String productJson,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        try {
            ProductUpdateChild request = objectMapper.readValue(productJson, ProductUpdateChild.class);

            if (images != null) {
                request.setImages(List.of(images));
            }

            Set<ConstraintViolation<ProductUpdateChild>> violations = validator.validate(request);

            if (!violations.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                for (ConstraintViolation<?> violation : violations) {
                    errors.put(violation.getPropertyPath().toString(), violation.getMessage());
                }

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ResponseData.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Dữ liệu không hợp lệ")
                                .data(errors)
                                .build());
            }

            productService.updateChildProduct(id, request);

            return ResponseEntity.ok(
                    ResponseData.<Void>builder()
                            .status(HttpStatus.OK.value())
                            .message("Cập nhật sản phẩm con thành công")
                            .build()
            );

        } catch (AttributeValueDuplicate | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseData.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("Lỗi hệ thống, vui lòng thử lại sau")
                            .build());
        }
    }






    @GetMapping("/searchg")
    public ResponseData<List<ProductResponse>> productSearchByAttribute(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String categoryName) {

        ProductSearchRequest searchRequest = new ProductSearchRequest(
                name, minPrice, maxPrice, sportType, supplierName, categoryName
        );

        List<ProductResponse> products = productService.searchProduct(searchRequest);

        return ResponseData.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm thành công")
                .data(products)
                .build();
    }



    @GetMapping("/{id}")
    public ResponseData<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            ProductResponse productResponse = productService.findById(id);
            return ResponseData.<ProductResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy sản phẩm thành công")
                    .data(productResponse)
                    .build();
        } catch (ErrorException e) {
            return ResponseData.<ProductResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Sản phẩm không tồn tại")
                    .build();
        }
    }


    @GetMapping("/parent/{parentId}")
    public ResponseData<List<ProductResponse>> getProductsByParentId(@PathVariable Long parentId) {
        List<ProductResponse> products = productService.findByParentId(parentId);

        if (products.isEmpty()) {
            return ResponseData.<List<ProductResponse>>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Không tìm thấy sản phẩm con")
                    .data(products)
                    .build();
        }
        return ResponseData.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm thành công")
                .data(products)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseData<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteSoft(id);
        return ResponseData.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Xoá mềm sản phẩm thành công")
                .build();
    }


    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProductsByAttribute(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String categoryName) {

        ProductSearchRequest searchRequest = new ProductSearchRequest(
                name,
                minPrice,
                maxPrice,
                sportType,
                supplierName,
                categoryName
        );

        Page<ProductResponse> productResponses = productService.searchProductsByAttribute(page, size, searchRequest);
        Map<String, Object> response = PageUtils.createPageResponse(productResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all products", description = "Retrieve a paginated list of products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of products"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size) {
        Page<ProductResponse> productResponses = productService.getAllProducts(page, size);
        Map<String, Object> response = PageUtils.createPageResponse(productResponses);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteProducts(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        productService.delete(ids);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/finByNameProductChild/{name}")
    public List<ProductResponse> finByNameProductChild(@PathVariable("name") String name){
        return productService.finByNameProductChild(name);
    }

    @GetMapping("/findChildProductsByCate/{id}")
    public List<ProductResponse> finChildProByCateId(@PathVariable("id") Long id){
        return productService.finChildProByCateId(id);
    }

}
