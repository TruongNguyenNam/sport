package com.example.storesports.core.admin.Carrier.controller;

import com.example.storesports.core.admin.Carrier.payload.CarrierResponse;
import com.example.storesports.core.admin.category.payload.CategoryResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.carrier.CarrierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/carrier")
@RequiredArgsConstructor
public class CarrierController {
    private final CarrierService carrierService;

    @GetMapping()
    public ResponseData<List<CarrierResponse>> getAllCarriers(){
        List<CarrierResponse> carrierResponses = carrierService.getAll();
        return ResponseData.<List<CarrierResponse>>builder()
                .status(HttpStatus.OK.value()) // log
                .message("lấy danh sách đơn vị vận chuyển thành công") //
                .data(carrierResponses)
                .build();
    }
}
