package com.example.storesports.service.admin.address;

import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.entity.Address;

import java.util.List;

public interface AddressService {

    List<AddressResponse> getAll();

}
