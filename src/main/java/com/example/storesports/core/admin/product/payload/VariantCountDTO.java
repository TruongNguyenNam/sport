package com.example.storesports.core.admin.product.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VariantCountDTO {
    private Long parentProductId;
    private Long variantCount;

    public VariantCountDTO(Long parentProductId, Long variantCount) {
        this.parentProductId = parentProductId;
        this.variantCount = variantCount;
    }
}
