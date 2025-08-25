package com.example.storesports.core.admin.attribute_value.payload;

import com.example.storesports.entity.Product;
import com.example.storesports.entity.ProductAttribute;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductAttributeValueResponse {
    private Long id;

    private Long attributeId;
    private String productName;

    private String attributeName;

    private String value;

}
