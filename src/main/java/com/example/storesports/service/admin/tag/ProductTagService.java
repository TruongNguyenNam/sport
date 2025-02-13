package com.example.storesports.service.admin.tag;

import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.core.admin.tag.payload.ProductTagRequest;
import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductTagService {

    Page<ProductTagResponse> getAllTags(int page, int size);

    ProductTagResponse saveOrUpdateTag(ProductTagRequest productTagRequest, Long id);

//    List<SupplierResponse> findByName(String name);

    void deleteTag(List<Long> id);

    ProductTagResponse findById(Long id);



}
