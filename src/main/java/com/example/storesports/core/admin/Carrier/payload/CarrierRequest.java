package com.example.storesports.core.admin.Carrier.payload;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CarrierRequest {


    private String name;

    private String description;

    private Boolean deleted;
}
