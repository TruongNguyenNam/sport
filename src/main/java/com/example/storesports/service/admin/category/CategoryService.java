package com.example.storesports.service.admin.category;

import com.example.storesports.core.admin.category.payload.CategoryRequest;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.entity.Category;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> findAllCategories();

   // CategoryResponse saveOrUpdateCategory(CategoryRequest categoryRequest, Long id);

    CategoryResponse updateCategory(CategoryRequest categoryRequest, Long id);
    CategoryResponse saveCategory(CategoryRequest categoryRequest);
    List<CategoryResponse> findByName(String name);

    void deleteCategory(List<Long> id);

    CategoryResponse findById(Long id);

//    List<CategoryResponse> findAllCategoriesdáhdha();


}
