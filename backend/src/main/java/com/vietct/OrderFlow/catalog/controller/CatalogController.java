package com.vietct.OrderFlow.catalog.controller;

import java.util.UUID;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.dto.ProductResponseDTO;
import com.vietct.OrderFlow.catalog.service.CatalogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@Validated
public class CatalogController {

    private static final Logger log = LoggerFactory.getLogger(CatalogController.class);

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public Page<ProductResponseDTO> getProducts(
            @RequestParam(name = "page", defaultValue = "0")
            @Min(0) int page,

            @RequestParam(name = "size", defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(name = "sort", defaultValue = "createdAt,DESC")
            String sort,

            @RequestParam(name = "categoryId", required = false)
            UUID categoryId
    ) {
        log.info("GET /products page={} size={} sort={} categoryId={}",
                page, size, sort, categoryId);

        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);

        Page<Product> productPage = catalogService.getProducts(categoryId, pageable);

        return productPage.map(ProductResponseDTO::fromDomain);
    }

    @GetMapping("/{id}")
    public ProductResponseDTO getProductById(@PathVariable("id") UUID id) {
        log.info("GET /products/{}", id);

        Product product = catalogService.getProductById(id);
        return ProductResponseDTO.fromDomain(product);
    }

    private Sort parseSort(String sortParam) {
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        if (parts.length < 2) {
            return Sort.by(Sort.Direction.DESC, property);
        }
        Sort.Direction direction =
                "ASC".equalsIgnoreCase(parts[1].trim()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
