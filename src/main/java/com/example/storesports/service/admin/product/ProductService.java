package com.example.storesports.service.admin.product;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import org.springframework.data.domain.Page;

public interface ProductService {

    Page<ProductResponse> getAllProducts(int page, int size);

    ProductResponse addNewProduct(ProductRequest request);



}
