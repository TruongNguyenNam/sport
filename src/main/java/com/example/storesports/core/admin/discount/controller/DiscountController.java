package com.example.storesports.core.admin.discount.controller;

import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.core.admin.discount.payload.DiscountRequest;
import com.example.storesports.core.admin.discount.payload.DiscountResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.discount.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/discount")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;
    @PostMapping("/create")
    public DiscountResponse create(@RequestParam(name = "productIds",required=false) List<Long> productIds,@RequestParam(name = "categoryIds",required=false) List<Long> categoryIds,@RequestBody DiscountRequest discountRequest){
       return discountService.create(productIds, categoryIds, discountRequest);
    }
    @GetMapping
    public ResponseData<List<DiscountResponse>> getAll(){
        List<DiscountResponse> discountResponses = discountService.getAll();
        return ResponseData.<List<DiscountResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách thuộc tính thành công")
                .data(discountResponses)
                .build();
    }
}
