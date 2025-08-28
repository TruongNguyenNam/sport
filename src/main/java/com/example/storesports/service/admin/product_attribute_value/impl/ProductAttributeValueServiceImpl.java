package com.example.storesports.service.admin.product_attribute_value.impl;

import com.example.storesports.core.admin.attribute_value.payload.ProductAttributeValueResponse;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.entity.ProductAttributeValue;
import com.example.storesports.repositories.ProductAttributeValueRepository;
import com.example.storesports.service.admin.product_attribute_value.ProductAttributeValueService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductAttributeValueServiceImpl implements ProductAttributeValueService {

//    @Query("SELECT pav FROM ProductAttributeValue pav " +
//            "WHERE pav.product.parentProductId = :parentId " +
//            "OR pav.product.id = :parentId")
//    List<ProductAttributeValue> findByProductParentId(@Param("parentId") Long parentId);
        private final ProductAttributeValueRepository productAttributeValueRepository;

        private final ModelMapper modelMapper;

    @Override
    public List<ProductAttributeValueResponse> getAll(Long parentId) {
        // Gọi repository để lấy danh sách
        List<ProductAttributeValue> productAttributeValues = productAttributeValueRepository.findByProductParentId(parentId);

        if (productAttributeValues.isEmpty()) {
            return Collections.emptyList();
        }

        // Lọc ra sản phẩm biến thể đầu tiên
        Long firstVariantProductId = productAttributeValues.get(0).getProduct().getId();

        List<ProductAttributeValue> firstVariantValues = productAttributeValues.stream()
                .filter(pav -> pav.getProduct().getId().equals(firstVariantProductId))
                .collect(Collectors.toList());

        // Map sang DTO nhưng value để trống
        return firstVariantValues.stream()
                .map(pav -> {
                    ProductAttributeValueResponse response = new ProductAttributeValueResponse();
                    response.setId(pav.getId());
                    response.setAttributeId(pav.getAttribute().getId());
                    response.setAttributeName(pav.getAttribute().getName());
                    response.setValue(""); // để trống
                    return response;
                })
                .collect(Collectors.toList());

    }
}
