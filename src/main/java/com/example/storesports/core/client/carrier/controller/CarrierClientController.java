package com.example.storesports.core.client.carrier.controller;

import com.example.storesports.core.admin.carrier.payload.CarrierResponse;
import com.example.storesports.core.client.carrier.payload.CarrierClientResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.client.carrier.CarrierClientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping(value = "/api/v1/client/carrier")
@Validated
@RequiredArgsConstructor
@Tag(name = "carrier", description = "Endpoints for managing carrier")
@Slf4j
public class CarrierClientController {

    private final CarrierClientService carrierClientService;

    @GetMapping()
    public ResponseData<List<CarrierClientResponse>> getAllCarriers(){
        List<CarrierClientResponse> carrierResponses = carrierClientService.getAllCarrier();
        return ResponseData.<List<CarrierClientResponse>>builder()
                .status(HttpStatus.OK.value()) // log
                .message("lấy danh sách đơn vị vận chuyển thành công")
                .data(carrierResponses)
                .build();
    }

}
