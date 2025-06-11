package com.example.storesports.service.admin.coupon_usage.impl;

import com.example.storesports.core.admin.coupon_usage.payload.CouponUsageResponse;
import com.example.storesports.entity.Coupon;
import com.example.storesports.entity.CouponUsage;
import com.example.storesports.entity.User;
import com.example.storesports.repositories.CouponRepository;
import com.example.storesports.repositories.CouponUsageRepository;
import com.example.storesports.repositories.UserRepository;
import com.example.storesports.service.admin.coupon_usage.CouponUsageService;
import com.example.storesports.infrastructure.email.EmailService; // Thêm import này
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponUsageServiceImpl implements CouponUsageService {

    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService; // Thêm dòng này

    @Override
    public List<CouponUsageResponse> getCouponsForCustomer(Long customerId) {
        if (customerId == null) {
            return Collections.emptyList();
        }
        List<CouponUsage> couponUsages = couponUsageRepository.findByUserIdAndDeletedFalse(customerId);
        return couponUsages.stream()
                .map(couponUsage -> modelMapper.map(couponUsage, CouponUsageResponse.class)).collect(Collectors.toList());
    }

    @Override
    public CouponUsageResponse addCouponToCustomer(Long userId, Long couponId) {
        Optional<CouponUsage> existing = couponUsageRepository.findByUserIdAndCouponIdAndDeletedFalse(userId, couponId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("User đã sở hữu coupon này!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        CouponUsage couponUsage = new CouponUsage();
        couponUsage.setUser(user);
        couponUsage.setCoupon(coupon);
        couponUsage.setUsed(false);
        couponUsage.setDeleted(false);

        CouponUsage saved = couponUsageRepository.save(couponUsage);
//
//        // Gửi email cho user nhận coupon
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            emailService.sendCouponAwardedEmail(
                    user.getEmail(),
                    coupon.getCouponName(),
                    coupon.getCodeCoupon(),
                    coupon.getExpirationDate().toString(),
                    coupon.getDiscountAmount() + "đ"
            );
        }

        return modelMapper.map(saved, CouponUsageResponse.class);
    }

    @Override
    public List<CouponUsageResponse> addCouponToMultipleCustomers(List<Long> userIds, Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        List<CouponUsageResponse> responses = userIds.stream().map(userId -> {
            Optional<CouponUsage> existing = couponUsageRepository.findByUserIdAndCouponIdAndDeletedFalse(userId, couponId);
            if (existing.isPresent()) {
                return null;
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            CouponUsage couponUsage = new CouponUsage();
            couponUsage.setUser(user);
            couponUsage.setCoupon(coupon);
            couponUsage.setUsed(false);
            couponUsage.setDeleted(false);
            CouponUsage saved = couponUsageRepository.save(couponUsage);

            //   Gửi email cho user nhận coupon
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                emailService.sendCouponAwardedEmail(
                        user.getEmail(),
                        coupon.getCouponName(),
                        coupon.getCodeCoupon(),
                        coupon.getExpirationDate().toString(),
                        coupon.getDiscountAmount() + "đ"
                );
            }

            return modelMapper.map(saved, CouponUsageResponse.class);
        }).filter(r -> r != null).collect(Collectors.toList());
        return responses;
    }
}