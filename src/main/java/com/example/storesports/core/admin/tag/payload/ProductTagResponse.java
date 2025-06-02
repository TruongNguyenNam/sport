package com.example.storesports.core.admin.tag.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductTagResponse {
    private Long id;

    private String name;

    private String description;


}
