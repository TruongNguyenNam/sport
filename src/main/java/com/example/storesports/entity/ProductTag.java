package com.example.storesports.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Entity
@Table(name = "ProductTag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTag extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private Boolean deleted;

    @OneToMany(mappedBy = "tag",cascade = CascadeType.REMOVE)
    private List<ProductTagMapping> productTagMappings;

}
