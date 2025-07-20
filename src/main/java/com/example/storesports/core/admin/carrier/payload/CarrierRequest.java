package com.example.storesports.core.admin.carrier.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CarrierRequest {


    private String name;

    private String description;

    private Boolean deleted;
}
