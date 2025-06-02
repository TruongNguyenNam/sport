package com.example.storesports.service.admin.coupon.impl;

import com.example.storesports.core.admin.coupon.payload.CouponRequest;
import com.example.storesports.core.admin.coupon.payload.CouponResponse;
import com.example.storesports.core.admin.supplier.payload.SupplierResponse;
import com.example.storesports.entity.Coupon;
import com.example.storesports.infrastructure.constant.CouponStatus;
import com.example.storesports.repositories.CouponRepository;
import com.example.storesports.repositories.CouponUsageRepository;
import com.example.storesports.service.admin.coupon.CouponService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final ModelMapper modelMapper;


    @Override
    public List<CouponResponse> getAll() {
        return List.of();
    }

    @Override
    public List<CouponResponse> getAllActiveCoupons() {
        List<Coupon> coupons = couponRepository.getAllActiveCoupons();
        if (coupons.isEmpty()) {
            throw new IllegalArgumentException("Nhà sản xuất bị trống" + coupons);
        }
        return coupons.stream().map(coupon -> {
            CouponResponse response = modelMapper.map(coupon, CouponResponse.class);
            long usedCount = couponUsageRepository.countByCouponId(coupon.getId());
            response.setUsedCount(usedCount);
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<CouponResponse> findByCodeCoupon(String codeCoupon) {
        List<Coupon> coupons = couponRepository.findByCodeCoupon(codeCoupon);
        return coupons.stream().map(coupon -> {
            CouponResponse response = modelMapper.map(coupon, CouponResponse.class);
            long usedCount = couponUsageRepository.countByCouponId(coupon.getId());
            response.setUsedCount(usedCount);
            return response;
        }).collect(Collectors.toList());
    }

    @Scheduled(cron = "0 */1 * * * ?") // 60,000 ms = 1 phút
    @Transactional
    public void updateExpiredCoupons() {
        System.out.println("Cập nhật coupon");
        List<Coupon> coupons = couponRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Coupon coupon : coupons) {
            boolean isExpiredTime = coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(now);
            boolean isOutOfQuantity = coupon.getQuantity() != null && coupon.getQuantity() <= 0;
            if ((isExpiredTime || isOutOfQuantity) && coupon.getCouponStatus() != CouponStatus.EXPIRED) {
                coupon.setCouponStatus(CouponStatus.EXPIRED);
                couponRepository.save(coupon);
            }
        }
    }

    // Hàm sinh code 8 ký tự
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public CouponResponse saveCoupon(CouponRequest couponRequest) {
        Coupon coupon = new Coupon();
        coupon.setCouponName(couponRequest.getCouponName());
        coupon.setCouponAmount(couponRequest.getCouponAmount());
        coupon.setCouponStatus(CouponStatus.ACTIVE);
        coupon.setQuantity(couponRequest.getQuantity());
        coupon.setStartDate(couponRequest.getStartDate());
        coupon.setExpirationDate(couponRequest.getExpirationDate());
        coupon.setDeleted(false);
        // Sinh mã code 8 ký tự và kiểm tra trùng
        String code;
        do {
            code = generateRandomCode(8);
        } while (couponRepository.existsByCodeCoupon(code));
        coupon.setCodeCoupon(code);
        Coupon couponSaved = couponRepository.save(coupon);
        CouponResponse response = modelMapper.map(couponSaved, CouponResponse.class);
        response.setUsedCount(0L); // Vừa tạo thì chưa có ai dùng
        return response;
    }

    @Override
    public CouponResponse updateCoupon(CouponRequest couponRequest, Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon với id: " + id));

        // Đếm số đã tặng (số bản ghi CouponUsage)
        long usedCount = couponUsageRepository.countByCouponId(coupon.getId());

        // Kiểm tra số lượng mới không nhỏ hơn số đã tặng
        if (couponRequest.getQuantity() < usedCount) {
            throw new IllegalArgumentException(
                    "Số lượng coupon không được nhỏ hơn số đã tặng cho khách hàng (" + usedCount + ")"
            );
        }

        coupon.setCouponName(couponRequest.getCouponName());
        coupon.setCouponAmount(couponRequest.getCouponAmount());
        coupon.setCouponStatus(CouponStatus.valueOf(couponRequest.getCouponStatus()));
        coupon.setQuantity(couponRequest.getQuantity());
        coupon.setStartDate(couponRequest.getStartDate());
        coupon.setExpirationDate(couponRequest.getExpirationDate());

        Coupon updatedCoupon = couponRepository.save(coupon);
        CouponResponse response = modelMapper.map(updatedCoupon, CouponResponse.class);
        response.setUsedCount(usedCount);
        return modelMapper.map(updatedCoupon, CouponResponse.class);
    }
    @Override
    public CouponResponse findById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon với id: " + id));
        CouponResponse response = modelMapper.map(coupon, CouponResponse.class);
        // Đếm số đã tặng
        long usedCount = couponUsageRepository.countByCouponId(coupon.getId());
        response.setUsedCount(usedCount);
        return response;
    }

}
