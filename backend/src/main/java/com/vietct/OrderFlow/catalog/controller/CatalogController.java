package com.vietct.OrderFlow.catalog.controller;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.dto.ProductResponseDTO;
import com.vietct.OrderFlow.catalog.dto.ProductSearchCriteria;
import com.vietct.OrderFlow.catalog.service.CatalogService;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@Validated
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }


    @GetMapping("/search")
    public Page<ProductResponseDTO> searchProducts(
            @RequestParam(name = "q", required = false) String text,
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "inStockOnly", required = false, defaultValue = "false") boolean inStockOnly,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,DESC") String sort
    ) {
        Sort sortSpec = parseSort(sort);

        Pageable pageable = PageRequest.of(page, size, sortSpec);

        ProductSearchCriteria criteria = new ProductSearchCriteria(
                text,
                categoryId,
                minPrice,
                maxPrice,
                inStockOnly
        );

        Page<Product> result = catalogService.searchProducts(criteria, pageable);

        return result.map(ProductResponseDTO::fromDomain);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = (parts.length > 1 && "ASC".equalsIgnoreCase(parts[1]))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
