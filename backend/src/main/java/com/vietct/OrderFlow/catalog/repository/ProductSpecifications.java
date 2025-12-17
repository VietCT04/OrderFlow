package com.vietct.OrderFlow.catalog.repository;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.dto.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> build(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // text search in name + description (case-insensitive)
            if (criteria.text() != null && !criteria.text().isBlank()) {
                String pattern = "%" + criteria.text().toLowerCase() + "%";

                Predicate byName = cb.like(cb.lower(root.get("name")), pattern);
                Predicate byDescription = cb.like(cb.lower(root.get("description")), pattern);

                predicates.add(cb.or(byName, byDescription));
            }

            // filter by category
            UUID categoryId = criteria.categoryId();
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            // price >= minPrice
            BigDecimal minPrice = criteria.minPrice();
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            // price <= maxPrice
            BigDecimal maxPrice = criteria.maxPrice();
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // in-stock-only flag
            Boolean inStockOnly = criteria.inStockOnly();
            if (Boolean.TRUE.equals(inStockOnly)) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
