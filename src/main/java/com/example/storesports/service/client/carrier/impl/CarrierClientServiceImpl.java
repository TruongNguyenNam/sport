package com.example.storesports.service.client.carrier.impl;

import com.example.storesports.core.admin.carrier.payload.CarrierResponse;
import com.example.storesports.core.client.carrier.payload.CarrierClientResponse;
import com.example.storesports.entity.Carrier;
import com.example.storesports.repositories.CarrierRepository;
import com.example.storesports.service.client.carrier.CarrierClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarrierClientServiceImpl implements CarrierClientService {

    private final CarrierRepository carrierRepository;

    private final ModelMapper modelMapper;

    @Override
    public List<CarrierClientResponse> getAllCarrier() {
        List<Carrier> carriers = carrierRepository.findAll();
        if(carriers.isEmpty()){
            throw new IllegalArgumentException("danh sách không có đơn vị vận chuyển");
        }
        return carriers.stream()
                .map(carrier -> modelMapper.map(carrier, CarrierClientResponse.class))
                .collect(Collectors.toList());
    }
}
