package com.example.storesports.service.admin.coupon.impl;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.repositories.CouponRepository;
import com.example.storesports.repositories.ProductAttributeRepository;
import com.example.storesports.service.admin.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final ModelMapper modelMapper;


    @Override
    public List<CouponResponse> getAll() {
        List<Coupon> coupons = couponRepository.findAll();
        if(coupons.isEmpty()){
            throw new IllegalArgumentException("danh sách phiếu giam bị trống"+coupons);
        }

        return coupons.stream()
                .map(coupon -> modelMapper.map(coupon, CouponResponse.class))
                .collect(Collectors.toList());
    }
}
