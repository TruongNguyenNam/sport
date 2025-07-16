package com.example.storesports.core.admin.address.controller;

import com.example.storesports.core.admin.address.payload.AddressRequest;
import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.infrastructure.utils.ResponseData;
import com.example.storesports.service.admin.address.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    //cap nhat dia chi
    @PutMapping("/customers/{customerId}/addresses/{addressId}")
    public ResponseData<AddressResponse> updateAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @RequestBody AddressRequest request) {
        AddressResponse updated = addressService.updateAddressForCustomer(customerId, addressId, request);
        return ResponseData.<AddressResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Cập nhật địa chỉ thành công")
                .data(updated)
                .build();
    }

    //xoa mem dia chi
    @DeleteMapping("/customers/{customerId}/addresses/{addressId}")
    public ResponseData<AddressResponse> deleteAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        AddressResponse deleted = addressService.softDeleteAddressForCustomer(customerId, addressId);
        return ResponseData.<AddressResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xoá địa chỉ thành công")
                .data(deleted)
                .build();
    }
}
