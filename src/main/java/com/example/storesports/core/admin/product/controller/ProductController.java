package com.example.storesports.core.admin.product.controller;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import com.example.storesports.entity.Product;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.service.admin.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "api/v1/admin/product")
@Validated
@RequiredArgsConstructor
@Tag(name = "Product", description = "Endpoints for managing products")
public class ProductController {
    private final ProductService productService;

//    @PostMapping(consumes = "multipart/form-data")
//    public ResponseEntity<ProductResponse> addProduct(@Valid @ModelAttribute ProductRequest productRequest) {
//        ProductResponse response = productService.createProductWithVariants(productRequest);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> addProduct(@Valid @ModelAttribute ProductRequest productRequest) {
        try {
            ProductResponse response = productService.createProductWithVariants(productRequest);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create product", e);
        }
    }
//    @PostMapping()
//    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest productRequest) {
//        ProductResponse response = productService.createProductWithVariants(productRequest);
//        return new ResponseEntity<>(response, HttpStatus.CREATED);
//    }



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

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateAProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest productRequest) {
        ProductResponse response = productService.updateProduct(productRequest,id);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
