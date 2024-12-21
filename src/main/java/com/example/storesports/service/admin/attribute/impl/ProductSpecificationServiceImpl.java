package com.example.storesports.service.admin.attribute.impl;
import com.example.storesports.core.admin.attribute.payload.ProductSpecificationResponse;
import com.example.storesports.entity.ProductSpecification;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.ProductSpecificationRepository;
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

    private final ProductSpecificationRepository productSpecificationRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<ProductSpecificationResponse> getAllProductAttribute(int page, int size) {
    int validatedPage = PageUtils.validatePageNumber(page);
    int validatedSize = PageUtils.validatePageSize(size, 2);
    Pageable pageable = PageRequest.of(validatedPage, validatedSize);
            Page<ProductSpecification> productSpecificationPage = productSpecificationRepository.findAll(pageable);
        if(productSpecificationPage.isEmpty()){
        return new PageImpl<>(Collections.emptyList(),pageable,0);
    }
        List<ProductSpecificationResponse> productSpecificationResponses = productSpecificationPage.getContent()
                .stream().map(productSpecification -> modelMapper.map(productSpecification,ProductSpecificationResponse.class))
                .toList();
        return new PageImpl<>(productSpecificationResponses,pageable,productSpecificationPage.getTotalElements());
    }


}
