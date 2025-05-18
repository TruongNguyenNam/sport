package com.example.storesports.service.admin.attribute.impl;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeRequest;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.entity.Supplier;
import com.example.storesports.infrastructure.exceptions.DuplicateEntityException;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductAttributeServiceImpl implements AttributeService {

    private final ProductAttributeRepository productAttributeRepository;
    private final ModelMapper modelMapper;
//    @Override
//    public Page<ProductAttributeResponse> getAllProductAttribute(int page, int size) {
//    int validatedPage = PageUtils.validatePageNumber(page);
//    int validatedSize = PageUtils.validatePageSize(size, 2);
//    Pageable pageable = PageRequest.of(validatedPage, validatedSize);
//            Page<ProductAttribute> productSpecificationPage = productAttributeRepository.findAll(pageable);
//        if(productSpecificationPage.isEmpty()){
//        return new PageImpl<>(Collections.emptyList(),pageable,0);
//    }
//        List<ProductAttributeResponse> productSpecificationResponses = productSpecificationPage.getContent()
//                .stream().map(productSpecification -> modelMapper.map(productSpecification, ProductAttributeResponse.class))
//                .toList();
//        return new PageImpl<>(productSpecificationResponses,pageable,productSpecificationPage.getTotalElements());
//    }

    @Override
    public List<ProductAttributeResponse> findAllProductAttribute() {
        List<ProductAttribute> productAttributes = productAttributeRepository.findAllProductAttribute();
        if(productAttributes.isEmpty()){
            throw new IllegalArgumentException("thuộc tính bị trống"+productAttributes);
        }

        return productAttributes.stream()
                .map(productAttribute -> modelMapper.map(productAttribute,ProductAttributeResponse.class))
                .collect(Collectors.toList());
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
    public ProductAttributeResponse save(ProductAttributeRequest productAttributeRequest) {
       ProductAttribute productAttribute = new ProductAttribute();
       if(productAttributeRepository.countProductAttribute(productAttributeRequest.getName())>0){
        throw new DuplicateEntityException("name tồn tại");
       }else{
           productAttribute.setName(productAttributeRequest.getName());
           productAttribute.setDescription(productAttributeRequest.getDescription());

       }
       ProductAttribute attributeSaved = productAttributeRepository.save(productAttribute);
       return modelMapper.map(attributeSaved,ProductAttributeResponse.class);
    }

    @Override
    public ProductAttributeResponse update(Long id, ProductAttributeRequest productAttributeRequest) {
       if(productAttributeRepository.countProductAttributeByNameAndIdNot(productAttributeRequest.getName(), id)>0){
           System.out.println(productAttributeRepository.countProductAttributeByNameAndIdNot(productAttributeRequest.getName(), id));
           throw new DuplicateEntityException("name đã tồn tại");

       }
        ProductAttribute productAttribute =productAttributeRepository.findById(id).orElseThrow(()->new RuntimeException("ko có id productAtribute này"));
        productAttribute.setName(productAttributeRequest.getName());
        productAttribute.setDescription(productAttributeRequest.getDescription());
        ProductAttribute attributeSaved = productAttributeRepository.save(productAttribute);
        return modelMapper.map(attributeSaved,ProductAttributeResponse.class);
    }

    @Override
    public ProductAttributeResponse findById(Long id) {
            ProductAttribute productAttribute = productAttributeRepository.findById(id).
                    orElseThrow(() -> new ErrorException("AttributeId is not found"));

            return modelMapper.map(productAttribute,ProductAttributeResponse.class);


    }

    @Override
    public List<ProductAttributeResponse> searchName(String name) {
        Specification<ProductAttribute> spec = Specification.where(null);
        spec = spec.and(AttributeSpecification.search(name));
        System.out.println(spec);
        List<ProductAttribute> attribute=productAttributeRepository.findAll(spec);
    return attribute.stream().map(n->modelMapper.map(n,ProductAttributeResponse.class)).collect(Collectors.toList());
    }


}
