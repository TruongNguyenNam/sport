package com.example.storesports.service.admin.address.impl;

import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.core.admin.attribute.payload.ProductAttributeResponse;
import com.example.storesports.entity.Address;
import com.example.storesports.entity.ProductAttribute;
import com.example.storesports.repositories.AddressRepository;
import com.example.storesports.service.admin.address.AddressService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService{

    private final AddressRepository addressRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<AddressResponse> getAll() {
        List<Address> addresses = addressRepository.findAll();
        if(addresses.isEmpty()){
            throw new IllegalArgumentException("thuộc tính bị trống"+addresses);
        }

        return addresses.stream()
                .map(address -> modelMapper.map(address,AddressResponse.class)).collect(Collectors.toList());
    }



}
