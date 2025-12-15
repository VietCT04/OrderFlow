package com.vietct.OrderFlow.catalog.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.vietct.OrderFlow.catalog.domain.Product;

public class ProductResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imagePath;
    private CategoryResponseDTO category;
    private Instant createdAt;
    private Instant updatedAt;

    public ProductResponseDTO() {
    }

    public ProductResponseDTO(UUID id,
                              String name,
                              String description,
                              BigDecimal price,
                              Integer stock,
                              String imagePath,
                              CategoryResponseDTO category,
                              Instant createdAt,
                              Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductResponseDTO fromDomain(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponseDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImagePath(),
                CategoryResponseDTO.fromDomain(product.getCategory()),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public CategoryResponseDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryResponseDTO category) {
        this.category = category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
