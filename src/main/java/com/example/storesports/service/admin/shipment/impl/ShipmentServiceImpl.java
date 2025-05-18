package com.example.storesports.service.admin.shipment.impl;

import com.example.storesports.core.admin.address.payload.AddressResponse;
import com.example.storesports.core.admin.shipment.payload.ShipmentResponse;
import com.example.storesports.entity.Shipment;


import com.example.storesports.repositories.ShipmentRepository;
import com.example.storesports.service.admin.shipment.ShipmentService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    private final ModelMapper modelMapper;


    @Override
    public List<ShipmentResponse> getAll() {
        List<Shipment> shipments = shipmentRepository.findAll();
        if(shipments.isEmpty()){
            throw new IllegalArgumentException("danh sách ship bị trống"+shipments);
        }

        return shipments.stream()
                .map(shipment -> modelMapper.map(shipment, ShipmentResponse.class))
                .collect(Collectors.toList());
            }
}
