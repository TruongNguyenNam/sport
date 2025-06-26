package com.example.storesports.core.client.product.controller;

import com.example.storesports.core.client.product.payload.ProductResponseClient;
import com.example.storesports.core.client.product.payload.ProductSearchClientRequest;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.product.ProductClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/client/product")
@Validated
@RequiredArgsConstructor
@Tag(name = "Product", description = "Endpoints for managing products")
@Slf4j
public class ProductClientController {
    private final ProductClientService productClientService;

    @GetMapping
    public ResponseData<List<ProductResponseClient>> getAllProducts() {
        List<ProductResponseClient> products = productClientService.getAllProduct();
        return ResponseData.<List<ProductResponseClient>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm thành công")
                .data(products)
                .build();
    }


    @GetMapping("/{id}")
    public ResponseData<List<ProductResponseClient>> findByParentProductId(@PathVariable(name = "id") Long id){
        List<ProductResponseClient> productResponseClients = productClientService.findByParentProductId(id);
        return  ResponseData.<List<ProductResponseClient>>builder()
                .status(HttpStatus.OK.value())
                .message("tìm kiếm danh sách sản phẩm theo parent_id thành công")
                .data(productResponseClients)
                .build();
    }

//    @GetMapping("/filter")
//    public ResponseData<List<ProductResponseClient>> findByCategoryName(@RequestParam(required = false) String category){
//        List<ProductResponseClient> result;
//
//        if (category != null) {
//            result = productClientService.findByCategoryName(category);
//        } else {
//            result = productClientService.getAllProduct();
//        }
//        return ResponseData.<List<ProductResponseClient>>builder()
//                .status(HttpStatus.OK.value())
//                .message("Tìm kiếm danh sách sản phẩm theo danh mục thành công")
//                .data(result)
//                .build();
//    }

    @GetMapping("/collection")
    public ResponseData<List<ProductResponseClient>> productSearchByAttributeClient(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String categoryName
    ) {
        // Tạo request object từ các query params
        ProductSearchClientRequest searchRequest = new ProductSearchClientRequest();
        searchRequest.setMinPrice(minPrice);
        searchRequest.setMaxPrice(maxPrice);
        searchRequest.setSportType(sportType);
        searchRequest.setSupplierName(supplierName);
        searchRequest.setCategoryName(categoryName);

        // Gọi service để lọc sản phẩm
        List<ProductResponseClient> products = productClientService.FilterProducts(searchRequest);
        log.info("danh sách sản phẩm"+ products);
        // Trả về response chuẩn
        return ResponseData.<List<ProductResponseClient>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm thành công")
                .data(products)
                .build();
    }












}
