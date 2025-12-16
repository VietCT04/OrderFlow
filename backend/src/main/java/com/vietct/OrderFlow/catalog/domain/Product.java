package com.vietct.OrderFlow.catalog.domain;

import com.vietct.OrderFlow.common.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @PositiveOrZero
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "image_path", length = 512)
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_product_category")
    )
    private Category category;

    public Product() {
        // JPA
    }

    public Product(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String imagePath,
            Category category
    ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Category getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
