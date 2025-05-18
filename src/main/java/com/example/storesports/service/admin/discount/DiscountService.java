package com.example.storesports.service.admin.discount;

import com.example.storesports.core.admin.discount.payload.DiscountRequest;
import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscountService {
public DiscountResponse create(List<Long> productIds, List<Long> categoryIds,DiscountRequest discountRequest);
List<DiscountResponse>getAll();
}
