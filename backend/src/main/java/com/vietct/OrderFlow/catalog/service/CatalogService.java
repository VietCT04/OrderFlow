package com.vietct.OrderFlow.catalog.service;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.dto.ProductSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CatalogService {

    Product getProductById(UUID id);

    Page<Product> getProducts(UUID categoryId, Pageable pageable);

    Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable);
}
