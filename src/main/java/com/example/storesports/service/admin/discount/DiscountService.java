package com.example.storesports.service.admin.discount;

import com.example.storesports.core.admin.discount.payload.DiscountRequest;
import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.entity.Category;
import com.example.storesports.infrastructure.constant.DiscountStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscountService {

 DiscountResponse create(DiscountRequest discountRequest);
 DiscountResponse update(Long id,DiscountRequest discountRequest);

List<DiscountResponse>getAll();
DiscountResponse finByDiscountId(Long id);
List<DiscountResponse> finByName(String name);
List<DiscountResponse>filterStatus(String discountStatus);
DiscountResponse updateStatus(Long id);


}
