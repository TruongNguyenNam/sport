package com.example.storesports.core.admin.supplier.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SupplierResponse {
    private Long id;

    private String name;
    private String description;
}
