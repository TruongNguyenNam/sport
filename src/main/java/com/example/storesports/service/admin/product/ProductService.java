package com.example.storesports.service.admin.product;

import com.example.storesports.core.admin.product.payload.*;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductAttributeValue;
import com.example.storesports.entity.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ProductService {
    Page<ProductResponse> getAllProducts(int page, int size);

    Page<ProductResponse> searchProductsByAttribute(int page, int size, ProductSearchRequest productSearchRequest);

    void delete(List<Long> id);

    ProductResponse findById(Long id);

   List<ProductResponse> findByParentId(Long parentId);
   void createProductWithVariants(List<ProductRequest> requests, MultipartFile[] images);

     void createProductWithVariantsV1(List<ProductRequest> requests, MultipartFile[] imagesFromController);
   List<ProductResponse> getAllParentProduct();

   List<ProductResponse> searchProduct(ProductSearchRequest productSearchRequest);

    List<ProductResponse> getAllChildProduct();
    void updateParentProduct(Long id, ProductUpdateParent request);


     void updateChildProductV2(Long id, ProductUpdateChild request);
    void updateChildProduct(Long id, ProductUpdateChild request);

    void deleteSoft(Long id);

    List<ProductResponse> finByNameProductChild(String name);


    List<ProductResponse> finChildProByCateId(Long id);

     void addVariantsToExistingProduct(AddProductChild child);

    void validateAttributesAndValues(Long productId, List<AddProductChild.ProductAttributeValue> newAttributes);


    List<VariantCountDTO> getVariantCounts();

}
