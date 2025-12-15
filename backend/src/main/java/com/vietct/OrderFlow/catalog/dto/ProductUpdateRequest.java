package com.vietct.OrderFlow.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * All fields optional; when non-null they will be updated.
 */
public class ProductUpdateRequest {

    @Size(max = 255)
    private String name;

    @Size(max = 4000)
    private String description;

    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal price;

    @Min(0)
    private Integer stock;

    @Size(max = 1024)
    private String imagePath;

    private UUID categoryId;

    public ProductUpdateRequest() {
    }

    // getters and setters…

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

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
}
