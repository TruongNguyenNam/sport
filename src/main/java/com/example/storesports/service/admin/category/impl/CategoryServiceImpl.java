package com.example.storesports.service.admin.category.impl;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.entity.Category;
import com.example.storesports.entity.Product;
import com.example.storesports.infrastructure.exceptions.ErrorException;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.CategoryRepository;
import com.example.storesports.repositories.ProductRepository;
import com.example.storesports.service.admin.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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


    //    @Override
//    public Page<CategoryResponse> getAllCategories(int page, int size) {
//        int validatedPage = PageUtils.validatePageNumber(page);
//        int validatedSize = PageUtils.validatePageSize(size, 2);
//        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
//        Page<Category> categoryPage = categoryRepository.findAll(pageable);
//        if(categoryPage.isEmpty()){
//            return new PageImpl<>(Collections.emptyList(),pageable,0);
//        }
//        List<CategoryResponse> categoryResponses = categoryPage.getContent().stream()
//                .map(category -> modelMapper.map(category,CategoryResponse.class)).
//               collect(Collectors.toList());
//        return new PageImpl<>(categoryResponses,pageable,categoryPage.getTotalElements());
//    }

    @Override
    public List<CategoryResponse> findAllCategories() {
        List<Category> categories = categoryRepository.findAllCategory();
        if(categories.isEmpty()){
            throw new IllegalArgumentException("danh mục bị trống"+categories);
        }

        return categories.stream()
                .map(category -> modelMapper.map(category,CategoryResponse.class))
                .collect(Collectors.toList());
    }




    @Override
    public CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with id " + id + " not found"));

        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory, CategoryResponse.class);
    }

    @Override
    public CategoryResponse saveCategory(CategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());

        Category savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryResponse.class);
    }


    @Override
    public List<CategoryResponse> findByName(String name) {
        List<Category> categories = categoryRepository.findAll(CategorySpecification.findByName(name));
        if (categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(category -> modelMapper.map(category,CategoryResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(List<Long> id) {
        List<Category> categories = categoryRepository.findAllById(id);
        if(!categories.isEmpty()){
                categoryRepository.deleteAllInBatch(categories);
        }
    }

    @Override
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ErrorException("Id category is not found"));

        return modelMapper.map(category,CategoryResponse.class);
    }



}
