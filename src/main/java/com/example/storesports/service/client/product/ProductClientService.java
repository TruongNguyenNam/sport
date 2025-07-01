package com.example.storesports.service.client.product;


import com.example.storesports.core.client.product.payload.ProductResponseClient;
import com.example.storesports.core.client.product.payload.ProductSearchClientRequest;

import java.util.List;

public interface ProductClientService {


    List<ProductResponseClient> getAllProduct();

    List<ProductResponseClient> findByParentProductId(Long id);


    List<ProductResponseClient> findByCategoryName(String name);


    List<ProductResponseClient> FilterProducts(ProductSearchClientRequest request);
  }
