package com.example.storesports.core.admin.shipment.payload;

import com.example.storesports.entity.Order;
import com.example.storesports.infrastructure.constant.ShipmentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
public class ShipmentResponse {
    private Long id;


    private String trackingNumber;
    private Date shipmentDate;

    private String shipmentStatus;

    private String carrier;

    private LocalDateTime estimatedDeliveryDate;
}
