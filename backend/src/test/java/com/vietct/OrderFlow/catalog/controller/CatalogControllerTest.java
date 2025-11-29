package com.vietct.OrderFlow.catalog.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.vietct.OrderFlow.catalog.domain.Category;
import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.exception.ProductNotFoundException;
import com.vietct.OrderFlow.catalog.service.CatalogService;
import com.vietct.OrderFlow.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@Import(GlobalExceptionHandler.class) // include your @RestControllerAdvice in the slice
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Spring Boot 3.4+ replacement for @MockBean
    @MockitoBean
    private CatalogService catalogService;

    /**
     * Build a stub Product (and nested Category) using Mockito mocks.
     * This avoids needing setters on your JPA entities.
     */
    private Product buildProduct(UUID id, String name) {
        Category category = Mockito.mock(Category.class);
        Mockito.when(category.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(category.getName()).thenReturn("Electronics");
        Mockito.when(category.getSlug()).thenReturn("electronics");
        Mockito.when(category.getDescription()).thenReturn("Electronics category");
        Mockito.when(category.getCreatedAt()).thenReturn(Instant.now());
        Mockito.when(category.getUpdatedAt()).thenReturn(Instant.now());

        Product product = Mockito.mock(Product.class);
        Mockito.when(product.getId()).thenReturn(id);
        Mockito.when(product.getName()).thenReturn(name);
        Mockito.when(product.getDescription()).thenReturn("Nice product");
        Mockito.when(product.getPrice()).thenReturn(new BigDecimal("99.99"));
        Mockito.when(product.getStock()).thenReturn(10);
        Mockito.when(product.getImagePath()).thenReturn(null);
        Mockito.when(product.getCategory()).thenReturn(category);
        Mockito.when(product.getCreatedAt()).thenReturn(Instant.now());
        Mockito.when(product.getUpdatedAt()).thenReturn(Instant.now());

        return product;
    }

    @Test
    @DisplayName("GET /products returns paged list of products")
    void getProducts_returnsPagedProducts() throws Exception {
        // given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Product p1 = buildProduct(id1, "Product A");
        Product p2 = buildProduct(id2, "Product B");

        List<Product> content = List.of(p1, p2);
        Page<Product> page = new PageImpl<>(content, PageRequest.of(0, 2), 2);

        Mockito.when(catalogService.getProducts(
                        eq(null),               // categoryId
                        any(Pageable.class)))   // pageable
                .thenReturn(page);

        // when + then
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "createdAt,DESC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // page metadata
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                // first product fields
                .andExpect(jsonPath("$.content[0].id").value(id1.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Product A"))
                .andExpect(jsonPath("$.content[0].category.name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /products/{id} returns 200 with product when found")
    void getProductById_existing_returns200() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        Product product = buildProduct(id, "Product A");

        Mockito.when(catalogService.getProductById(id))
                .thenReturn(product);

        // when + then
        mockMvc.perform(get("/products/{id}", id.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Product A"))
                .andExpect(jsonPath("$.category.name").value("Electronics"));
    }

    @Test
    @DisplayName("GET /products/{id} returns 404 with structured JSON when product is missing")
    void getProductById_missing_returns404() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        Mockito.when(catalogService.getProductById(id))
                .thenThrow(new ProductNotFoundException(id));

        // when + then
        mockMvc.perform(get("/products/{id}", id.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found: " + id))
                .andExpect(jsonPath("$.path").value("/products/" + id));
    }

    @Test
    @DisplayName("GET /products with invalid page parameter returns 400 validation error")
    void getProducts_invalidPage_returns400() throws Exception {
        // page < 0 violates @Min(0) in controller method
        mockMvc.perform(get("/products")
                        .param("page", "-1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
        // fieldErrors details depend on property path; we keep assertion generic
    }
}
