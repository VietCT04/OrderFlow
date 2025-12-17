package com.vietct.OrderFlow.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Criteria object for dynamic product search.
 * All fields are optional; null means "no filter".
 */
public record ProductSearchCriteria(
        String text,              // search in name/description
        UUID categoryId,          // filter by category
        BigDecimal minPrice,      // >= minPrice
        BigDecimal maxPrice,      // <= maxPrice
        Boolean inStockOnly       // true -> quantity > 0
) {
}
