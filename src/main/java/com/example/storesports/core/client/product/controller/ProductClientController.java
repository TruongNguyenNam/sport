package com.example.storesports.core.client.product.controller;

import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.client.product.payload.ProductResponseClient;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.product.ProductClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    













}
