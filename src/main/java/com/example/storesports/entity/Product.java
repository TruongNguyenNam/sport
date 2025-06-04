package com.example.storesports.entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends Auditable{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String description;
        private Double price;
       //gia goc
        private Double originalPrice;

        private Integer stockQuantity;
        private String sportType;
        private String sku;
        private Boolean deleted;

        @Column(name = "parent_product_id")
        private Long parentProductId;

        @ManyToOne
        @JoinColumn(name = "supplier_id")
        private Supplier supplier;

        @ManyToOne
        @JoinColumn(name = "category_id")
        private Category category;

        @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<ProductImage> images = new ArrayList<>();

        @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE,fetch = FetchType.LAZY,orphanRemoval = true)
        private List<ProductAttributeValue> productAttributeValues;


        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<ShoppingCartItem> shoppingCartItems;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<OrderItem> orderItems;

        @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
        private List<ProductTagMapping> productTagMappings;

        @OneToMany(mappedBy = "product",cascade = CascadeType.ALL)
        private List<ProductDiscountMapping> productDiscountMappings;


//        @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
//        private List<Inventory> inventories;

}
