package com.example.storesports.service.admin.carrier;

import com.example.storesports.core.admin.carrier.payload.CarrierResponse;
import com.example.storesports.core.admin.carrier.payload.CarrierRequest;
import java.util.List;

public interface CarrierService {

    List<CarrierResponse> getAll();

    CarrierResponse create(CarrierRequest carrierRequest);

}
