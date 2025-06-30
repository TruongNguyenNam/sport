package com.example.storesports.core.client.carrier.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CarrierClientResponse {
    private Long id;

    private String name;

    private String description;

    private Boolean deleted;

}
