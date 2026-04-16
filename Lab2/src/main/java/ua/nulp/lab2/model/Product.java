package ua.nulp.lab2.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Product {
    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
}