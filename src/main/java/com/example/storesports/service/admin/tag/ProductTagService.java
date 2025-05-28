package com.example.storesports.service.admin.tag;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.core.admin.tag.payload.ProductTagRequest;
import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import org.springframework.data.domain.Page;

import javax.swing.text.html.HTML;
import java.util.List;

public interface ProductTagService {

    Page<ProductTagResponse> getAllTags(int page, int size);

    ProductTagResponse saveOrUpdateTag(ProductTagRequest productTagRequest, Long id);

//    List<SupplierResponse> findByName(String name);
    ProductTagResponse updateTag(ProductTagRequest productTagRequest, Long id);
    ProductTagResponse saveTag(ProductTagRequest productTagRequest);

    List<ProductTagResponse> findAllTags();

    void deleteTag(List<Long> id);

    ProductTagResponse findById(Long id);



}
