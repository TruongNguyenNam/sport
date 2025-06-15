package com.example.storesports.service.admin.product;

import com.example.storesports.core.admin.product.payload.*;
import com.example.storesports.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    Page<ProductResponse> getAllProducts(int page, int size);

    Page<ProductResponse> searchProductsByAttribute(int page, int size, ProductSearchRequest productSearchRequest);

    void delete(List<Long> id);

    ProductResponse findById(Long id);

   List<ProductResponse> findByParentId(Long parentId);
   void createProductWithVariants(List<ProductRequest> requests, MultipartFile[] images);

   List<ProductResponse> getAllParentProduct();

   List<ProductResponse> searchProduct(ProductSearchRequest productSearchRequest);

    List<ProductResponse> getAllChildProduct();
    void updateParentProduct(Long id, ProductUpdateParent request);

    void updateChildProduct(Long id, ProductUpdateChild request);

    void deleteSoft(Long id);

    List<ProductResponse> finByNameProductChild(String name);


    List<ProductResponse> finChildProByCateId(Long id);

     void addVariantsToExistingProduct(AddProductChild child);

    void validateAttributesAndValues(Long productId, List<AddProductChild.ProductAttributeValue> newAttributes);

}
