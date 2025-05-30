package com.example.storesports.service.admin.coupon_usage.impl;

import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;
import com.example.storesports.entity.CouponUsage;
import com.example.storesports.repositories.CouponUsageRepository;
import com.example.storesports.service.admin.coupon_usage.CouponUsageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponUsageServiceImpl implements CouponUsageService {

    private final CouponUsageRepository couponUsageRepository;

    private final ModelMapper modelMapper;


    @Override
    public List<CouponUsageResponse> getCouponsForCustomer(Long customerId) {
        if (customerId == null) {
            return Collections.emptyList(); // Trả về danh sách rỗng cho khách vãng lai
        }
        List<CouponUsage> couponUsages = couponUsageRepository.findByUserIdAndDeletedFalse(customerId);
//        if(couponUsages.isEmpty()){
//            throw new IllegalArgumentException("khách hàng chưa có phiếu giảm giá");
//        }
        return couponUsages.stream()
                .map(couponUsage -> modelMapper.map(couponUsage, CouponUsageResponse.class)).collect(Collectors.toList());
    }




}
