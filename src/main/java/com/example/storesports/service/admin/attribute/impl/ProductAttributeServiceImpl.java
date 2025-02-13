package com.example.storesports.service.admin.attribute.impl;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeRequest;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.entity.Supplier;
import com.example.storesports.infrastructure.exceptions.ErrorException;
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
public class ProductAttributeServiceImpl implements AttributeService {

    private final ProductAttributeRepository productAttributeRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<ProductAttributeResponse> getAllProductAttribute(int page, int size) {
    int validatedPage = PageUtils.validatePageNumber(page);
    int validatedSize = PageUtils.validatePageSize(size, 2);
    Pageable pageable = PageRequest.of(validatedPage, validatedSize);
            Page<ProductAttribute> productSpecificationPage = productAttributeRepository.findAll(pageable);
        if(productSpecificationPage.isEmpty()){
        return new PageImpl<>(Collections.emptyList(),pageable,0);
    }
        List<ProductAttributeResponse> productSpecificationResponses = productSpecificationPage.getContent()
                .stream().map(productSpecification -> modelMapper.map(productSpecification, ProductAttributeResponse.class))
                .toList();
        return new PageImpl<>(productSpecificationResponses,pageable,productSpecificationPage.getTotalElements());
    }

    @Override
    public ProductAttributeResponse saveOrUpdateAttribute(ProductAttributeRequest productAttributeRequest, Long id) {
        ProductAttribute productAttribute;
        if(id != null){
            productAttribute = productAttributeRepository.findById(id)
                    .orElseThrow(() -> new ErrorException("Attribute with id" + id + "not found"));
        }else{
            productAttribute = new ProductAttribute();
        }

        productAttribute.setName(productAttributeRequest.getName());
        productAttribute.setDescription(productAttributeRequest.getDescription());

        ProductAttribute attributeSaved = productAttributeRepository.save(productAttribute);

        return modelMapper.map(attributeSaved,ProductAttributeResponse.class);


    }

    @Override
    public void deleteAttribute(List<Long> id) {

        List<ProductAttribute> productAttributes = productAttributeRepository.findAllById(id);
        if(!productAttributes.isEmpty()){
                productAttributeRepository.deleteAllInBatch(productAttributes);
        }


    }

    @Override
    public ProductAttributeResponse findById(Long id) {
            ProductAttribute productAttribute = productAttributeRepository.findById(id).
                    orElseThrow(() -> new ErrorException("AttributeId is not found"));

            return modelMapper.map(productAttribute,ProductAttributeResponse.class);


    }


}
