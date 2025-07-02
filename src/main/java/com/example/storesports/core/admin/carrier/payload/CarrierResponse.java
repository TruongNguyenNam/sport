package com.example.storesports.core.admin.carrier.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CarrierResponse {

    private Long id;

    private String name;

    private String description;

    private Boolean deleted;
}
