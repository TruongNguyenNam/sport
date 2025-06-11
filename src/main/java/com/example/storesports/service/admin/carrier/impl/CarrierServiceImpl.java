package com.example.storesports.service.admin.carrier.impl;

import com.example.storesports.core.admin.Carrier.payload.CarrierResponse;
import com.example.storesports.entity.Carrier;
import com.example.storesports.repositories.CarrierRepository;
import com.example.storesports.service.admin.carrier.CarrierService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarrierServiceImpl implements CarrierService {

    private final CarrierRepository carrierRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<CarrierResponse> getAll() {
        List<Carrier> carriers = carrierRepository.findAll();
        if(carriers.isEmpty()){
            throw new IllegalArgumentException("danh sách không có đơn vị vận chuyển");
        }
        return carriers.stream()
                .map(carrier -> modelMapper.map(carrier, CarrierResponse.class))
                .collect(Collectors.toList());
    }
}
