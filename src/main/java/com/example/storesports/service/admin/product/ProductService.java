package com.example.storesports.service.admin.product;

import com.example.storesports.core.admin.product.payload.ProductRequest;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.core.admin.product.payload.ProductSearchRequest;
import com.example.storesports.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    Page<ProductResponse> getAllProducts(int page, int size);

    Page<ProductResponse> searchProductsByAttribute(int page, int size, ProductSearchRequest productSearchRequest);

    void delete(List<Long> id);

    ProductResponse updateProduct(ProductRequest request,Long id);

    ProductResponse findById(Long id);

   List<ProductResponse> findByParentId(Long parentId);
     ProductResponse createProductWithVariants(ProductRequest productRequest);

}
