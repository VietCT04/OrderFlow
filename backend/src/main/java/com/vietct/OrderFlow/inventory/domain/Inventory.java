package com.vietct.OrderFlow.inventory.domain;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.common.model.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_inventory_product")
    )
    private Product product;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Long getVersion() {
        return version;
    }
}
