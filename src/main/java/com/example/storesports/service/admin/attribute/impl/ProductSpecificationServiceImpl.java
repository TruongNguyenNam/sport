package com.example.storesports.service.admin.attribute.impl;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.ProductAttributeRepository;
import com.example.storesports.service.admin.attribute.AttributeService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSpecificationServiceImpl implements AttributeService {

    private final ProductAttributeRepository productSpecificationRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<ProductAttributeResponse> getAllProductAttribute(int page, int size) {
    int validatedPage = PageUtils.validatePageNumber(page);
    int validatedSize = PageUtils.validatePageSize(size, 2);
    Pageable pageable = PageRequest.of(validatedPage, validatedSize);
            Page<ProductAttribute> productSpecificationPage = productSpecificationRepository.findAll(pageable);
        if(productSpecificationPage.isEmpty()){
        return new PageImpl<>(Collections.emptyList(),pageable,0);
    }
        List<ProductAttributeResponse> productSpecificationResponses = productSpecificationPage.getContent()
                .stream().map(productSpecification -> modelMapper.map(productSpecification, ProductAttributeResponse.class))
                .toList();
        return new PageImpl<>(productSpecificationResponses,pageable,productSpecificationPage.getTotalElements());
    }


}
