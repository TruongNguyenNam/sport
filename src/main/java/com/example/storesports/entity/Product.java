package com.example.storesports.entity;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        private Integer stockQuantity;
        private String sportType;
        private String material;
        private String size;
        private String color;
        private String sku;

        @ManyToOne
        @JoinColumn(name = "supplier_id")
        private Supplier supplier;

        @ManyToOne
        @JoinColumn(name = "category_id")
        private Category category;

        @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
        private List<ProductSupplierMapping> productSupplierMappings;

        @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
        private List<ProductSportTypeMapping> productSportTypeMappings;

        @OneToMany(mappedBy = "product", cascade = CascadeType.REMOVE)
        private List<ProductSpecificationOption> productSpecificationOptions;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<ProductReview> productReviews;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<ProductTagMapping> productTagMappings;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<ShoppingCartItem> shoppingCartItems;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<OrderItem> orderItems;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<ProductImage> productImages;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<ProductDiscountMapping> productDiscountMappings;

        @OneToMany(mappedBy = "product",cascade = CascadeType.REMOVE)
        private List<InventoryAdjustment> inventoryAdjustments;


}
