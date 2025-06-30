package com.example.storesports.service.client.coupon_usage.impl;

import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;
import com.example.storesports.core.client.coupon_usage.payload.CouponUsageClientResponse;
import com.example.storesports.entity.CouponUsage;
import com.example.storesports.repositories.CouponRepository;
import com.example.storesports.repositories.CouponUsageRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.client.coupon_usage.CouponUsageClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponUsageClientServiceImpl implements CouponUsageClientService {
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final ModelMapper modelMapper;
    @Override
    public List<CouponUsageClientResponse> getAllCouponUsageByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<CouponUsage> couponUsages = couponUsageRepository.findByUserIdAndDeletedFalse(userId);
        return couponUsages.stream()
                .map(couponUsage -> modelMapper.map(couponUsage, CouponUsageClientResponse.class)).collect(Collectors.toList());
    }













}
