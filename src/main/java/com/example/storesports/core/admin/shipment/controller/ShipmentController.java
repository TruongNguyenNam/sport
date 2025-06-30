package com.example.storesports.core.admin.shipment.controller;

import com.example.storesports.core.admin.shipment.payload.ShipmentResponse;
import com.example.storesports.entity.Shipment;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.shipment.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/shipment")
@Validated
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    @GetMapping
    public ResponseData<List<ShipmentResponse>> getAllShipment(){
        List<ShipmentResponse> shipmentResponses = shipmentService.getAll();
        return ResponseData.<List<ShipmentResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách ship thành công giá thành công")
                .data(shipmentResponses)
                .build();
    }




}
