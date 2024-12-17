package com.example.storesports.service.admin.product;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {

    Page<ProductResponse> getAllProducts(int page, int size);

    ProductResponse addNewProduct(ProductRequest request);


    Page<ProductResponse> searchProductsByAttribute(int page, int size, ProductSearchRequest productSearchRequest);

    void delete(List<Long> id);





}
