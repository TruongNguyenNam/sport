package com.example.storesports.core.admin.product.controller;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import com.example.storesports.infrastructure.exceptions.ErrorException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/admin/product")
@Validated
@RequiredArgsConstructor
@Tag(name = "Product", description = "Endpoints for managing products")
@Slf4j
public class ProductController {
    private final ProductService productService;
    private final ObjectMapper objectMapper;


    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseData<Void> addProduct(
            @RequestParam("products") String productsJson,
            @RequestParam(value = "parentImages", required = false) MultipartFile[] parentImages,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {

        log.info("Received JSON data: {}", productsJson);
        log.info("Received {} parent images", (parentImages != null ? parentImages.length : 0));
        log.info("Received {} images", (images != null ? images.length : 0));

        try {
            List<ProductRequest> requests = objectMapper.readValue(
                    productsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ProductRequest.class)
            );

            if (requests.isEmpty()) {
                throw new IllegalArgumentException("Danh sách yêu cầu sản phẩm trống!");
            }

            ProductRequest request = requests.get(0);
            if (parentImages != null && parentImages.length > 0) {
                request.setParentImages(new ArrayList<>(Arrays.asList(parentImages)));
            } else {
                request.setParentImages(new ArrayList<>());
            }

            if (images != null && images.length > 0) {
                int index = 0;
                for (ProductRequest.ProductVariant variant : request.getVariants()) {
                    if (index < images.length) {
                        variant.setImages(new ArrayList<>(List.of(images[index])));
                        index++;
                    } else {
                        variant.setImages(new ArrayList<>());
                    }
                }
            } else {
                for (ProductRequest.ProductVariant variant : request.getVariants()) {
                    variant.setImages(new ArrayList<>());
                }
            }

            productService.createProductWithVariants(requests, images);

            return ResponseData.<Void>builder()
                    .status(HttpStatus.OK.value())
                    .message("Thêm sản phẩm thành công")
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Error parsing product request", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");
        } catch (Exception e) {
            log.error("Unexpected error while creating product", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
        }
    }


    @GetMapping("/parent")
    public ResponseData<List<ProductResponse>> getAllParentProducts() {
        List<ProductResponse> products = productService.getAllParentProduct();
        return ResponseData.<List<ProductResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm cha thành công")
                .data(products)
                .build();
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

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        try {
            ProductResponse productResponse = productService.findById(id);
            return new ResponseEntity<>(productResponse, HttpStatus.OK);
        } catch (ErrorException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
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

//        System.out.println("Page: " + page + " | Size: " + size);
//        System.out.println("Total elements: " + productResponses.getTotalElements());
//        System.out.println("conyent" + productResponses.getContent());
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

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<ProductResponse>> getProductsByParentId(@PathVariable Long parentId) {
        List<ProductResponse> products = productService.findByParentId(parentId);
        return ResponseEntity.ok(products);
    }




}
