package com.example.storesports.service.admin.discount.impl;

import com.example.storesports.core.admin.discount.payload.DiscountRequest;
import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.core.admin.product.payload.ProductResponse;
import com.example.storesports.entity.Auditable;
import com.example.storesports.entity.Discount;
import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductDiscountMapping;
import com.example.storesports.infrastructure.constant.DiscountStatus;
import com.example.storesports.infrastructure.exceptions.ErrorException;
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

import java.math.BigDecimal;
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
    public DiscountResponse create(DiscountRequest discountRequest) {
        Discount discount = new Discount();
        LocalDateTime now=LocalDateTime.now();
        discount.setName(discountRequest.getName());

        discount.setDiscountPercentage(discountRequest.getPercentValue());
        if(!discountRequest.getStartDate().isBefore(now)){
            discount.setStartDate(discountRequest.getStartDate());
        }
        else{
            throw new ErrorException("ko dược nhập  ngày bắt đầu  quá khứ");
        }

        if(discountRequest.getEndDate().isBefore(now)){
            throw new ErrorException("ko dược nhập ngay kêt thúc bé hơn hiện thời gian hiện tại");

        }
        else{
            discount.setEndDate(discountRequest.getEndDate());
        }
        if (!discountRequest.getStartDate().isBefore(discountRequest.getEndDate())) {
            throw new ErrorException("ko được nhập ngày kết thúc bé hơn ngày bắt đầu");
        }
        if(discountRequest.getPercentValue()>100){
            throw new ErrorException("giảm giá không được nhập quá 100% bạn đang nhập "+discountRequest.getPercentValue()+"%");
        }
        else if(discountRequest.getPercentValue() <0){
            throw new ErrorException("giảm giá không được bé hơn 0% bạn đang nhập "+discountRequest.getPercentValue()+"%");
        }
        else{
            discount.setPriceThreshold(discountRequest.getPriceThreshold());
        }

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


        if (discountRequest.getProductIds() != null && !discountRequest.getProductIds().isEmpty()) {
            applicableProducts.addAll(productRepository.findAllById(discountRequest.getProductIds()));

        }

        boolean noProductSelected = Boolean.FALSE.equals(discountRequest.getApplyToAll())

                && (discountRequest.getProductIds() == null || discountRequest.getProductIds().isEmpty());

        if (noProductSelected) {
            throw new ErrorException("phải có ít nhất 1 sản phẩm");
        }

        Double priceThreshold = discountRequest.getPriceThreshold();

        if (discount.getStatus() == DiscountStatus.ACTIVE) {
            List<Discount> activeDiscounts ;
            for (Product p : applicableProducts) {
                activeDiscounts =discountRepository.findActiveDiscountsByProductId(p.getId(),DiscountStatus.ACTIVE);
                Optional<Discount> discountCheck =activeDiscounts .stream().max(Comparator.comparing(discount1 -> discount1.getDiscountPercentage()));

                if (p.getParentProductId()==null) {
                    continue;
                }
                if(discountCheck.isPresent()) {
                    if (p.getParentProductId()!=null && p.getOriginalPrice() == null) {
                        p.setOriginalPrice(p.getPrice());
                    }
                    if (p.getParentProductId() != null && p.getOriginalPrice() >= priceThreshold) {
                        Discount maxDiscount = discountCheck.get();
                        Double discountPercent = maxDiscount.getDiscountPercentage();
                        Double discountedPrice = p.getOriginalPrice() - (p.getOriginalPrice() * discountPercent / 100);
                        p.setPrice(discountedPrice);
                        productRepository.save(p);
                        boolean exists = productDiscountMappingRepository.existsByProductAndDiscount(p, discount);
                        if (!exists) {
                            ProductDiscountMapping mapping = new ProductDiscountMapping();
                            mapping.setProduct(p);
                            mapping.setDiscount(discount);
                            productDiscountMappingRepository.save(mapping);
                        }

                    }

                }
                else{
                    throw new ErrorException("ko co gia tri");
                }


            }
        }else if (discount.getStatus() == DiscountStatus.PENDING) {
            boolean hasValidProduct = false;

            for (Product p : applicableProducts) {
                Double price = p.getOriginalPrice() != null ? p.getOriginalPrice() : p.getPrice();

                if (p.getParentProductId() != null && price != null && price >= priceThreshold) {
                    ProductDiscountMapping mapping = new ProductDiscountMapping();
                    mapping.setDiscount(discount);
                    mapping.setProduct(p);
                    productDiscountMappingRepository.save(mapping);
                    hasValidProduct = true;
                }
            }

            if (!hasValidProduct) {
                throw new ErrorException("Không có sản phẩm nào đủ điều kiện với ngưỡng giá này");
            }
        }

        return mapToResponse(discount);
    }

        @Override
        public DiscountResponse update(Long id, DiscountRequest discountRequest) {
            Discount discount = discountRepository.findById(id)
                    .orElseThrow(() -> new ErrorException("ko có id discount này"));

            LocalDateTime now = LocalDateTime.now();


            // Date validation



            if (discountRequest.getPercentValue() > 100) {
                throw new ErrorException("giảm giá không được nhập quá 100% bạn đang nhập " + discountRequest.getPercentValue() + "%");
            }


            if (now.isBefore(discount.getStartDate())) {
                discount.setStatus(DiscountStatus.PENDING);
            } else if (now.isAfter(discount.getEndDate())) {
                discount.setStatus(DiscountStatus.EXPIRED);
            } else {
                discount.setStatus(DiscountStatus.ACTIVE);
            }

            discountRepository.save(discount);


            List<ProductDiscountMapping> oldMappings = productDiscountMappingRepository.findByDiscount(discount);
            Set<Product> oldProducts = oldMappings.stream()
                    .map(ProductDiscountMapping::getProduct)
                    .collect(Collectors.toSet());

            boolean noProductSelected = Boolean.FALSE.equals(discountRequest.getApplyToAll())

                    && (discountRequest.getProductIds() == null || discountRequest.getProductIds().isEmpty());

            if (noProductSelected) {
                throw new ErrorException("phải có ít nhất 1 sản phẩm");
            }

            // ✅ Xác định sản phẩm mới được áp dụng
            Set<Product> applicableProducts = new HashSet<>();
            if (discountRequest.getProductIds() != null && !discountRequest.getProductIds().isEmpty()) {
                applicableProducts.addAll(productRepository.findAllById(discountRequest.getProductIds()));
            }

            // ✅ Rollback cho sản phẩm KHÔNG còn nằm trong danh sách mới
            Set<Long> newProductIds = applicableProducts.stream().map(Product::getId).collect(Collectors.toSet());
            for (Product oldProduct : oldProducts) {
                if (!newProductIds.contains(oldProduct.getId()) && oldProduct.getParentProductId() != null) {
                    if (oldProduct.getOriginalPrice() != null) {
                        oldProduct.setPrice(oldProduct.getOriginalPrice());
                        productRepository.save(oldProduct);
                    }
                }
            }

            // ✅ Xoá tất cả mapping cũ
//            productDiscountMappingRepository.deleteByDiscount(discount);

            Double priceThreshold = discountRequest.getPriceThreshold();

            if (discount.getStatus() == DiscountStatus.ACTIVE) {
                throw new ErrorException("Không thể sửa discount đang hoạt động");
//                productDiscountMappingRepository.deleteByDiscount(discount);
//                Map<Long, List<Discount>> productActiveDiscountsMap = applicableProducts.stream()
//                        .filter(p -> p.getParentProductId() != null)
//                        .collect(Collectors.toMap(
//                                Product::getId,
//                                p -> discountRepository.findActiveDiscountsByProductId(p.getId(), DiscountStatus.ACTIVE)
//                                        .stream()
//                                        .filter(d -> !d.getId().equals(discount.getId()))
//                                        .collect(Collectors.toList())
//                        ));
//
//                for (Product p : applicableProducts) {
//                    if (p.getParentProductId() != null) {
//                        if (p.getOriginalPrice() == null) {
//                            p.setOriginalPrice(p.getPrice());
//                            productRepository.save(p);
//                        }
//
//                        if (p.getOriginalPrice() >= priceThreshold) {
//                            Optional<Double> maxOtherDiscount = productActiveDiscountsMap.getOrDefault(p.getId(), Collections.emptyList())
//                                    .stream()
//                                    .map(Discount::getDiscountPercentage)
//                                    .max(Double::compare);
//
//                            double effectiveDiscount = discount.getDiscountPercentage();
//                            if (maxOtherDiscount.isPresent() && maxOtherDiscount.get() > effectiveDiscount) {
//                                effectiveDiscount = maxOtherDiscount.get();
//                            }
//
//                            Double discountedPrice = p.getOriginalPrice() - (p.getOriginalPrice() * effectiveDiscount / 100);
//                            p.setPrice(discountedPrice);
//                            productRepository.save(p);
//
//
//                            ProductDiscountMapping mapping = new ProductDiscountMapping();
//                            mapping.setProduct(p);
//                            mapping.setDiscount(discount);
//                            productDiscountMappingRepository.save(mapping);
//                        } else {
//                            throw new ErrorException("không có sản phẩm nào được ap dụng với ngưỡng giá này vui lòng xem lại");
//                        }
//
//                    }
//                }
            }
            else if (discount.getStatus() == DiscountStatus.PENDING||discount.getStatus()==DiscountStatus.EXPIRED) {

                LocalDateTime newStart = discountRequest.getStartDate();
                LocalDateTime newEnd = discountRequest.getEndDate();

                if (newStart.isAfter(newEnd)) {
                    throw new ErrorException("Ngày bắt đầu không được sau ngày kết thúc");
                }
                if (!newStart.isAfter(now)) {
                    throw new ErrorException("Ngày bắt đầu phải sau thời điểm hiện tại");
                }


                discount.setStartDate(newStart);
                discount.setEndDate(newEnd);

                if (now.isBefore(discount.getStartDate())) {
                    discount.setStatus(DiscountStatus.PENDING);
                } else if (now.isAfter(discount.getEndDate())) {
                    discount.setStatus(DiscountStatus.EXPIRED);
                } else {
                    discount.setStatus(DiscountStatus.ACTIVE);
                }



                List<Product> validProducts = new ArrayList<>();
                discount.setName(discountRequest.getName());
                discount.setDiscountPercentage(discountRequest.getPercentValue());
                for (Product p : applicableProducts) {
                    if (p.getParentProductId() != null) {
                        double productPrice = p.getOriginalPrice() != null ? p.getOriginalPrice() : p.getPrice();
                        if (productPrice >= priceThreshold) {
                            validProducts.add(p);
                        }
                    }
                }

                if (validProducts.isEmpty()) {
                    // Không xóa gì cả → dữ liệu cũ vẫn nguyên
                    throw new ErrorException("Không có sản phẩm nào đủ điều kiện áp ngưỡng giá này, vui lòng sửa lại");
                }

                // Chỉ xóa khi chắc chắn có sản phẩm hợp lệ
                productDiscountMappingRepository.deleteByDiscount(discount);

                discount.setPriceThreshold(discountRequest.getPriceThreshold());
                discountRepository.save(discount);

                for (Product p : validProducts) {
                    ProductDiscountMapping mapping = new ProductDiscountMapping();
                    mapping.setDiscount(discount);
                    mapping.setProduct(p);
                    productDiscountMappingRepository.save(mapping);
                }
            }


//            else if (discount.getStatus() == DiscountStatus.EXPIRED) {
//                LocalDateTime newStart = discountRequest.getStartDate();
//                LocalDateTime newEnd = discountRequest.getEndDate();
//
//                if (newStart.isAfter(newEnd)) {
//                    throw new ErrorException("Ngày bắt đầu không được sau ngày kết thúc");
//                }if(discountRequest.getStartDate().isBefore(now)){
//                    throw new ErrorException("ko dược nhập  ngày bắt đầu  quá khứ");
//                }
//
//
//                if (newEnd.isBefore(now)) {
//                    throw new ErrorException("Ngày kết thúc mới phải sau thời điểm hiện tại");
//                }
//
//                discount.setStartDate(newStart);
//                discount.setEndDate(newEnd);
//                if (now.isBefore(discount.getStartDate())) {
//                    discount.setStatus(DiscountStatus.PENDING);
//                } else if (now.isAfter(discount.getEndDate())) {
//                    discount.setStatus(DiscountStatus.EXPIRED);
//                } else {
//                    discount.setStatus(DiscountStatus.ACTIVE);
//                }
//
//                discountRepository.save(discount);
//            }


            return mapToResponse(discount);
        }


    @Override
    public List<DiscountResponse> getAll() {
        List<Discount>discounts=discountRepository.findAll();
        return discounts.stream()
                .sorted(Comparator.comparing
                        (Discount::getCreatedDate).reversed())
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    @Override
    public DiscountResponse finByDiscountId(Long id) {
        Discount discount=discountRepository.findById(id).orElseThrow(()->new ErrorException("ko co id discount này"));
        List<ProductDiscountMapping> mappings=productDiscountMappingRepository.findByDiscountId(id);
        List<ProductResponse> productResponses=new ArrayList<>();
        for(ProductDiscountMapping mapping:mappings){
            Product p=mapping.getProduct();
            ProductResponse productResponse=new ProductResponse();
            productResponse.setId(p.getId());
            productResponse.setPrice(p.getOriginalPrice());
            productResponse.setName(p.getName());

            productResponses.add(productResponse);

        }
        DiscountResponse dto = new DiscountResponse();
        dto.setId(discount.getId());
        dto.setName(discount.getName());
        dto.setDiscountPercentage(discount.getDiscountPercentage() + " %");
        dto.setCountProduct(productResponses.size());
        dto.setStartDate(discount.getStartDate());
        dto.setEndDate(discount.getEndDate());
        dto.setProductResponses(productResponses);
        dto.setPriceThreshold(discount.getPriceThreshold());



        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(discount.getStartDate())) {
            dto.setStatus("Chưa bắt đầu");
        } else if (!now.isAfter(discount.getEndDate())) {
            dto.setStatus("Đang áp dụng");
        } else {
            dto.setStatus("Đã hết hạn");
        }

        return dto;

    }

    @Override
    public List<DiscountResponse> finByName(String name) {
        List<Discount> discounts=discountRepository.findByNameContaining(name);
        return discounts.stream().map(d->mapToResponse(d)).collect(Collectors.toList());
    }

    @Override
    public List<DiscountResponse> filterStatus(String discountStatus) {
        List<Discount> discounts = discountRepository.findAll();
        if (discountStatus.equals("ACTIVE")) {
            System.out.println("active");
            List<Discount> active = discounts.stream().filter(dc -> dc.getStatus().name().equalsIgnoreCase(discountStatus))
                    .collect(Collectors.toList());
            return active.stream().sorted(Comparator.comparing(Discount::getCreatedDate).reversed()).map(d -> mapToResponse(d)).collect(Collectors.toList());
        } else if (discountStatus.equals("PENDING")) {
            System.out.println("pending");
            List<Discount> active = discounts.stream().filter(dc -> dc.getStatus().name().equalsIgnoreCase(discountStatus))
                    .collect(Collectors.toList());
            return active.stream().sorted(Comparator.comparing(Discount::getCreatedDate).reversed()).map(d -> mapToResponse(d)).collect(Collectors.toList());
        } else if (discountStatus.equals("INACTIVE")) {
            System.out.println("cc");
            List<Discount> active = discounts.stream().filter(dc -> dc.getStatus().name().equalsIgnoreCase(discountStatus))
                    .collect(Collectors.toList());
            return active.stream().sorted(Comparator.comparing(Discount::getCreatedDate).reversed()).map(d -> mapToResponse(d)).collect(Collectors.toList());
        }
        else if(discountStatus.equals("EXPIRED")){
            System.out.println("cc");
            List<Discount> active = discounts.stream().filter(dc -> dc.getStatus().name().equalsIgnoreCase(discountStatus))
                    .collect(Collectors.toList());
            return active.stream().sorted(Comparator.comparing(Discount::getEndDate).reversed()).map(d -> mapToResponse(d)).collect(Collectors.toList());
        }
        else{
            throw new ErrorException("ko co status nay");
        }



    }@Override
    public DiscountResponse updateStatus(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new ErrorException("ko co id discount nay"));

        List<ProductDiscountMapping> mappings = productDiscountMappingRepository.findByDiscount(discount);
        Set<Product> products = mappings.stream()
                .map(ProductDiscountMapping::getProduct)
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();

        if (discount.getStatus().equals(DiscountStatus.ACTIVE)) {
            discount.setStatus(DiscountStatus.INACTIVE);

            for (Product product : products) {
                if (product.getParentProductId() != null) {

                    if (product.getOriginalPrice() == null) {
                        product.setOriginalPrice(product.getPrice());
                        productRepository.save(product);
                    }


                    List<Discount> discounts = discountRepository.findActiveDiscountsByProductId(
                            product.getId(), DiscountStatus.ACTIVE);

                    Optional<Discount> discountMax = discounts.stream()
                            .filter(d -> !d.getId().equals(discount.getId()))
                            .filter(d -> product.getOriginalPrice() >= d.getPriceThreshold())
                            .max(Comparator.comparing(Discount::getDiscountPercentage));

                    if (discountMax.isPresent()) {
                        double percent = discountMax.get().getDiscountPercentage();
                        double newPrice = product.getOriginalPrice() - (product.getOriginalPrice() * percent / 100);
                        product.setPrice(newPrice);
                    } else {
                        product.setPrice(product.getOriginalPrice());
                    }

                    productRepository.save(product);
                }
            }

        } else if (discount.getStatus().equals(DiscountStatus.INACTIVE)) {
            if (!now.isBefore(discount.getStartDate()) && !now.isAfter(discount.getEndDate())) {
                discount.setStatus(DiscountStatus.ACTIVE);

                for (Product product : products) {
                    if (product.getParentProductId() != null) {

                        if (product.getOriginalPrice() == null) {
                            product.setOriginalPrice(product.getPrice());
                        }

                        List<Discount> discounts = discountRepository.findActiveDiscountsByProductId(
                                product.getId(), DiscountStatus.ACTIVE);
                        discounts.add(discount);

                        Optional<Discount> discountMax = discounts.stream()
                                .filter(d -> product.getOriginalPrice() >= d.getPriceThreshold())
                                .max(Comparator.comparing(Discount::getDiscountPercentage));

                        if (discountMax.isPresent()) {
                            double percent = discountMax.get().getDiscountPercentage();
                            double newPrice = product.getOriginalPrice() - (product.getOriginalPrice() * percent / 100);
                            product.setPrice(newPrice);
                        } else {
                            product.setPrice(product.getOriginalPrice());
                        }

                        productRepository.save(product);
                    }
                }
            }
        } else {
            throw new ErrorException("discount đang ko ở trong trạng thái hoạt động");
        }

        discountRepository.save(discount);
        return mapToResponse(discount);
    }


    @Transactional
    @Scheduled(cron = "0 */1 * * * ?") // chạy mỗi phút

    public void rollbackExpiredDiscounts() {

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

                List<Discount> activeDiscounts = discountRepository.findActiveDiscountsByProductId(p.getId(), DiscountStatus.ACTIVE);

                // Lấy discount có phần trăm lớn nhất
                Optional<Discount> bestDiscount = activeDiscounts.stream()
                        .max(Comparator.comparing(Discount::getDiscountPercentage));

                if (bestDiscount.isPresent()) {
                    Discount newDiscount = bestDiscount.get();


                    if (p.getParentProductId()!=null && p.getOriginalPrice() == null) {
                        if (p.getParentProductId()!=null && p.getPrice() >= newDiscount.getPriceThreshold()) {
                            p.setOriginalPrice(p.getPrice());
                            Double discountedPrice = p.getOriginalPrice() - (p.getOriginalPrice() * newDiscount.getDiscountPercentage() / 100);
                            p.setPrice(discountedPrice);
                            productRepository.save(p);


                            boolean exists = productDiscountMappingRepository.existsByProductAndDiscount(p, newDiscount);
                            if (!exists) {
                                ProductDiscountMapping mapping1 = new ProductDiscountMapping();
                                mapping1.setProduct(p);
                                mapping1.setDiscount(newDiscount);
                                productDiscountMappingRepository.save(mapping1);
                            }

                        }
                    }
                    else{
                        if (p.getParentProductId()!=null && p.getOriginalPrice() >= newDiscount.getPriceThreshold()) {
                            Double discountedPrice = p.getOriginalPrice() - (p.getOriginalPrice() * newDiscount.getDiscountPercentage() / 100);
                            p.setPrice(discountedPrice);
                            productRepository.save(p);

                            boolean exists = productDiscountMappingRepository.existsByProductAndDiscount(p, newDiscount);
                            if (!exists) {
                                ProductDiscountMapping mapping1 = new ProductDiscountMapping();
                                mapping1.setProduct(p);
                                mapping1.setDiscount(newDiscount);
                                productDiscountMappingRepository.save(mapping1);
                            }

                        }


                    }
                }
            }
            discount.setStatus(DiscountStatus.EXPIRED);
            discountRepository.save(discount);

        }
    }
    @Transactional
    @Scheduled(cron = "0 */1 * * * ?")
    public void applyPendingDiscounts() {
        LocalDateTime now = LocalDateTime.now();
        List<Discount> pendingDiscounts = discountRepository.findPendingDiscountsToActivate(now);
        for (Discount discount : pendingDiscounts) {
            if (discount.getStartDate() != null && !discount.getStartDate().isAfter(now)) {
                discount.setStatus(DiscountStatus.ACTIVE);
                discountRepository.save(discount);
                Double percent = discount.getDiscountPercentage();
                Double priceThreshold = discount.getPriceThreshold();
                List<Product> applicableProducts = productRepository.findProductsByDiscountId(discount.getId());
                for (Product p : applicableProducts) {
                    if (p.getParentProductId()!=null && p.getOriginalPrice() == null) {
                        p.setOriginalPrice(p.getPrice());
                    }
                    if (p.getParentProductId()!=null && p.getOriginalPrice() >= priceThreshold) {
                        Double discountedPrice = p.getOriginalPrice() - (p.getOriginalPrice() * percent / 100);
                        p.setPrice(discountedPrice);
                        productRepository.save(p);
                    }
                    boolean exists = productDiscountMappingRepository.existsByProductAndDiscount(p, discount);
                    if (!exists) {
                        ProductDiscountMapping mapping = new ProductDiscountMapping();
                        mapping.setProduct(p);
                        mapping.setDiscount(discount);
                        productDiscountMappingRepository.save(mapping);
                    }


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
        discountResponse.setStartDate(discount.getStartDate());
        discountResponse.setEndDate(discount.getEndDate());
        discountResponse.setCountProduct(countProduct);


        LocalDateTime now = LocalDateTime.now();
        if (discount.getStatus().equals(DiscountStatus.PENDING)) {
            discountResponse.setStatus("Chưa bắt đầu");
        } else if (discount.getStatus().equals(DiscountStatus.ACTIVE)) {
            discountResponse.setStatus("Đang áp dụng");
        }
        else if(discount.getStatus().equals(DiscountStatus.INACTIVE)){
            discountResponse.setStatus("Tạm ngưng");
        }
        else {
            discountResponse.setStatus("Đã hết hạn");
        }
        return discountResponse;
    }

}


