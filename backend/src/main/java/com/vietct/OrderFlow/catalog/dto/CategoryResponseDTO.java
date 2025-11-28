package com.vietct.OrderFlow.catalog.dto;

import java.util.UUID;

public class CategoryResponseDTO {

    private UUID id;
    private String name;
    private String slug;
    private String description;

    public CategoryResponseDTO() {
    }

    public CategoryResponseDTO(UUID id, String name, String slug, String description) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    public static CategoryResponseDTO fromDomain(com.vietct.OrderFlow.catalog.domain.Category category) {
        if (category == null) {
            return null;
        }
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription()
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
