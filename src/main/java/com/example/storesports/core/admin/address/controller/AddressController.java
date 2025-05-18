package com.example.storesports.core.admin.address.controller;

import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.address.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;


    @GetMapping
    public ResponseData<List<AddressResponse>> getAllAddress(){
        List<AddressResponse> addressResponses = addressService.getAll();
        return ResponseData.<List<AddressResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("lấy danh sách địa chỉ thành công")
                .data(addressResponses)
                .build();
    }



}
