package com.example.storesports.service.admin.tag.impl;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.tag.payload.ProductTagRequest;
import com.example.storesports.core.admin.tag.payload.ProductTagResponse;
import com.example.storesports.entity.ProductTag;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.ProductTagRepository;
import com.example.storesports.service.admin.tag.ProductTagService;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductTagServiceImpl implements ProductTagService {

    private final ProductTagRepository productTagRepository;

    private final ModelMapper modelMapper;

    @Override
    public Page<ProductTagResponse> getAllTags(int page, int size) {
        int validatedPage = PageUtils.validatePageNumber(page);
        int validatedSize = PageUtils.validatePageSize(size, 2);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Page<ProductTag> productTagPage = productTagRepository.findAll(pageable);
        if(productTagPage.isEmpty()){
            return new PageImpl<>(Collections.emptyList(),pageable,0);
        }
        List<ProductTagResponse> productTagResponses = productTagPage.getContent().stream()
                        .map(productTag -> modelMapper.map(productTag,ProductTagResponse.class)).
                toList();
        return new PageImpl<>(productTagResponses,pageable,productTagPage.getTotalElements());
    }

    @Override
    public ProductTagResponse saveOrUpdateTag(ProductTagRequest productTagRequest, Long id) {
        ProductTag productTag;
        if(id != null){
            productTag = productTagRepository.findById(id)
                    .orElseThrow(() -> new ErrorException("tag is not found" + id));
        }else{
            productTag = new ProductTag();
        }

        productTag.setName(productTagRequest.getName());

        ProductTag tagSaved = productTagRepository.save(productTag);

        return modelMapper.map(tagSaved,ProductTagResponse.class);
    }

    @Override
    public List<ProductTagResponse> findAllTags() {
        List<ProductTag> productTagResponses = productTagRepository.findAllTags();
        if(productTagResponses.isEmpty()){
            throw new IllegalArgumentException("tag bị trống"+ productTagResponses);
        }

        return productTagResponses.stream()
                .map(productTag -> modelMapper.map(productTag,ProductTagResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTag(List<Long> id) {
    List<ProductTag> productTags = productTagRepository.findAllById(id);
    if(!productTags.isEmpty()){
            productTagRepository.deleteAllInBatch(productTags);
    }
    }

    @Override
    public ProductTagResponse findById(Long id) {
            ProductTag productTag = productTagRepository.
                    findById(id).orElseThrow(() -> new ErrorException("TagId is not found"));
            return modelMapper.map(productTag,ProductTagResponse.class);

    }


}
