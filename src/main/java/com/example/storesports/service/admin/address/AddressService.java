package com.example.storesports.service.admin.address;

import com.example.storesports.core.admin.address.payload.AddressRequest;
import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.entity.Address;

import java.util.List;

public interface AddressService {

    List<AddressResponse> getAll();

    AddressResponse addAddressToCustomer(Long customerId, AddressRequest request);

    AddressResponse updateAddressForCustomer(Long customerId, Long addressId, AddressRequest request);

    AddressResponse softDeleteAddressForCustomer(Long customerId, Long addressId);

}
