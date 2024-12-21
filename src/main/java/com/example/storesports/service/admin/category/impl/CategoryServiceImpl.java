package com.example.storesports.service.admin.category.impl;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.entity.Category;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.CategoryRepository;
import com.example.storesports.service.admin.category.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    @Override
    public Page<CategoryResponse> getAllCategories(int page, int size) {
        int validatedPage = PageUtils.validatePageNumber(page);
        int validatedSize = PageUtils.validatePageSize(size, 2);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        if(categoryPage.isEmpty()){
            return new PageImpl<>(Collections.emptyList(),pageable,0);
        }
        List<CategoryResponse> categoryResponses = categoryPage.getContent().stream()
                .map(category -> modelMapper.map(category,CategoryResponse.class)).
               collect(Collectors.toList());
        return new PageImpl<>(categoryResponses,pageable,categoryPage.getTotalElements());
    }

    @Override
    public CategoryResponse saveOrUpdateCategory(CategoryRequest categoryRequest, Long id) {
        return null;
    }

    @Override
    public Page<CategoryResponse> findByName(String name) {
        return null;
    }
}