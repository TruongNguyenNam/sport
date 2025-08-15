package com.example.storesports.core.admin.attribute.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
public class ProductAttributeRequest {
//    private Long id;
    @NotBlank(message = "Name cannot be blank")
    @Length(min = 1,max = 20,message = "length >1 and <20")
    private String name;
    private String description;
}
