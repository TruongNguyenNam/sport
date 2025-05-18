package com.example.storesports.service.admin.discount.impl;

import com.example.storesports.core.admin.discount.payload.DiscountRequest;
import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.entity.Category;
import com.example.storesports.entity.Discount;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductDiscountMapping;
import com.example.storesports.infrastructure.constant.DiscountStatus;
import com.example.storesports.repositories.CategoryRepository;
import com.example.storesports.repositories.DiscountRepository;
import com.example.storesports.repositories.ProductDiscountMappingRepository;
import com.example.storesports.repositories.ProductRepository;
import com.example.storesports.service.admin.discount.DiscountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {
    private final DiscountRepository discountRepository;
    private final ProductDiscountMappingRepository productDiscountMappingRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public DiscountResponse create(List<Long> productIds, List<Long> categoryIds, DiscountRequest discountRequest) {
        Discount discount = new Discount();
        discount.setName(discountRequest.getName());
        System.out.println("cc");
        discount.setDiscountPercentage(discountRequest.getPercentValue());
        discount.setStartDate(discountRequest.getStartDate());
        discount.setEndDate(discountRequest.getEndDate());
        discount.setPriceThreshold(discountRequest.getPriceThreshold());
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(discount.getStartDate())) {
            discount.setStatus(DiscountStatus.PENDING);
        } else if (now.isAfter(discount.getEndDate())) {
            discount.setStatus(DiscountStatus.EXPIRED);
        } else {
            discount.setStatus(DiscountStatus.ACTIVE);
        }
        discountRepository.save(discount);

        Set<Product> applicableProducts = new HashSet<>();



        if (Boolean.TRUE.equals(discountRequest.getApplyToAll())) {
            applicableProducts.addAll(productRepository.findAll());

        }

        else if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(categoryIds);
            for (Category category : categories) {
                applicableProducts.addAll(category.getProducts());

            }

        }

        else if (productIds != null && !productIds.isEmpty()) {
            applicableProducts.addAll(productRepository.findAllById(productIds));

        }


        Double percent = discountRequest.getPercentValue();
        Double priceThreshold = discountRequest.getPriceThreshold();

        if (discount.getStatus() == DiscountStatus.ACTIVE) {
            for (Product p : applicableProducts) {

                if (p.getParentProductId() != null && p.getPrice() >= priceThreshold) {
                    if (p.getOriginalPrice() == null) {
                        p.setOriginalPrice(p.getPrice());
                    }
                    Double discountedPrice = p.getPrice() - (p.getPrice() * percent / 100);
                    p.setPrice(discountedPrice);
                    productRepository.save(p);
                    ProductDiscountMapping mapping = new ProductDiscountMapping();
                    mapping.setDiscount(discount);
                    mapping.setProduct(p);
                    productDiscountMappingRepository.save(mapping);

                }
            }
        } else if (discount.getStatus() == DiscountStatus.PENDING) {
            for (Product p : applicableProducts) {
                ProductDiscountMapping mapping = new ProductDiscountMapping();
                mapping.setDiscount(discount);
                mapping.setProduct(p);
                productDiscountMappingRepository.save(mapping);
            }
        }

        return mapToResponse(discount);
    }

    @Override
    public List<DiscountResponse> getAll() {
        List<Discount>discounts=discountRepository.findAll();
        return discounts.stream().map(d->mapToResponse(d)).collect(Collectors.toList());
    }

        @Transactional
        @Scheduled(cron = "0 */1 * * * ?") // chạy mỗi phút
        public void rollbackExpiredDiscounts() {
            Date now = new Date();
            System.out.println("chay");
            List<Discount> expiredDiscounts = discountRepository.findExpiredDiscounts(LocalDateTime.now());

            for (Discount discount : expiredDiscounts) {
                List<ProductDiscountMapping> mappings = productDiscountMappingRepository.findByDiscount(discount);

                for (ProductDiscountMapping mapping : mappings) {
                    Product p = mapping.getProduct();
                    if (p.getOriginalPrice() != null && p.getOriginalPrice() > 0) {
                        p.setPrice(p.getOriginalPrice());
                        p.setOriginalPrice(null);
                        productRepository.save(p);
                    }
                    productDiscountMappingRepository.delete(mapping);

                }
                discount.setStatus(DiscountStatus.EXPIRED);
                discountRepository.save(discount);

            }
        }
    @Transactional
    @Scheduled(cron = "0 */1 * * * ?")
    public void applyPendingDiscounts() {
        System.out.println("chay pending");
        LocalDateTime now = LocalDateTime.now();
        List<Discount> pendingDiscounts = discountRepository.findPendingDiscountsToActivate(now);
        System.out.println(pendingDiscounts.size());
        for (Discount discount : pendingDiscounts) {
            if (discount.getStartDate() != null && !discount.getStartDate().isAfter(now)) {
                discount.setStatus(DiscountStatus.ACTIVE);
                discountRepository.save(discount);

                Double percent = discount.getDiscountPercentage();


                System.out.println("đến đây r");
                List<Product> applicableProducts = productRepository.findProductsByDiscountId(discount.getId());
                System.out.println(applicableProducts.size());
                for (Product p : applicableProducts) {
                    if (p.getOriginalPrice() == null) {
                        p.setOriginalPrice(p.getPrice());
                    }
                    Double discountedPrice = p.getPrice() - (p.getPrice() * percent / 100);
                    System.out.println("ll");
                    p.setPrice(discountedPrice);
                    productRepository.save(p);
                    ProductDiscountMapping mapping = new ProductDiscountMapping();
                    mapping.setDiscount(discount);
                    mapping.setProduct(p);
                    productDiscountMappingRepository.save(mapping);
                }
            }
        }
    }

    public DiscountResponse mapToResponse(Discount discount) {

        int countProduct= productDiscountMappingRepository.countByDiscount(discount.getId());
        DiscountResponse discountResponse = new DiscountResponse();
        discountResponse.setId(discount.getId());
        discountResponse.setName(discount.getName());
        discountResponse.setDiscountPercentage(discount.getDiscountPercentage()+" %");
//        discountResponse.setStartDate(discount.getStartDate());
//        discountResponse.setEndDate(discount.getEndDate());
        discountResponse.setCountProduct(countProduct);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(discount.getStartDate())) {
        discountResponse.setStatus("Chưa bắt đầu");
    } else if (!now.isAfter(discount.getEndDate())) {
        discountResponse.setStatus("Đang áp dụng");
    } else {
        discountResponse.setStatus("Đã hết hạn");
    }
    return discountResponse;
}

    }


