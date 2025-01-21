package com.example.storesports.service.admin.supplier.impl;

import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierRequest;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.entity.Category;
import com.example.storesports.entity.Supplier;
import com.example.storesports.infrastructure.utils.PageUtils;
import com.example.storesports.repositories.SupplierRepository;
import com.example.storesports.service.admin.supplier.SupplierService;
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
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<SupplierResponse> getAllSupplier(int page, int size) {
        int validatedPage = PageUtils.validatePageNumber(page);
        int validatedSize = PageUtils.validatePageSize(size, 2);
        Pageable pageable = PageRequest.of(validatedPage, validatedSize);
        Page<Supplier> supplierPage = supplierRepository.findAll(pageable);
        if (supplierPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<SupplierResponse> supplierResponses = supplierPage.getContent().stream()
                .map(supplier -> modelMapper.map(supplier, SupplierResponse.class))
                .collect(Collectors.toList());
        return new PageImpl<>(supplierResponses, pageable, supplierPage.getTotalElements());
    }

    @Override
    public SupplierResponse saveOrUpdateSupplier(SupplierRequest supplierRequest, Long id) {
        Supplier supplier;

        if (id != null) {
            supplier = supplierRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Category with id " + id + " not found"));
        } else {
            supplier = new Supplier();
        }
        supplier.setName(supplierRequest.getName());
        supplier.setDescription(supplierRequest.getDescription());

        Supplier supplierSaved = supplierRepository.save(supplier);

        return modelMapper.map(supplierSaved, SupplierResponse.class);

    }

    @Override
    public List<SupplierResponse> findByName(String name) {
        List<Supplier> suppliers = supplierRepository.findAll(SupplierSpecification.findByName(name));
            if(suppliers.isEmpty()){
                    return Collections.emptyList();
            }

        return suppliers.stream()
                .map(supplier -> modelMapper.map(supplier,SupplierResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSupplier(List<Long> id) {
        List<Supplier> suppliers = supplierRepository.findAllById(id);
        if(!suppliers.isEmpty()){
            supplierRepository.deleteAllInBatch(suppliers);
        }
    }

}

