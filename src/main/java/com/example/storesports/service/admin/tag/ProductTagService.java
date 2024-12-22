package com.example.storesports.service.admin.tag;

import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import org.springframework.data.domain.Page;

public interface ProductTagService {

    Page<ProductTagResponse> getAllTags(int page, int size);





}
