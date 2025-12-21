package com.vietct.OrderFlow.catalog.service;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.dto.ProductSearchCriteria;
import com.vietct.OrderFlow.catalog.exception.CategoryNotFoundException;
import com.vietct.OrderFlow.catalog.exception.ProductNotFoundException;
import com.vietct.OrderFlow.catalog.repository.CategoryRepository;
import com.vietct.OrderFlow.catalog.repository.ProductRepository;
import com.vietct.OrderFlow.catalog.repository.ProductSpecifications;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Cacheable(cacheNames = "productById", key = "#id")
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Cacheable(
            cacheNames = "frontPageProducts",
            key = "'default'",
            condition = "#categoryId == null && #pageable.pageNumber == 0"
    )
    public Page<Product> getProducts(UUID categoryId, Pageable pageable) {
        if (categoryId == null) {
            return productRepository.findAll(pageable);
        }

        boolean categoryExists = categoryRepository.existsById(categoryId);
        if (!categoryExists) {
            throw new CategoryNotFoundException(categoryId);
        }

        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        Specification<Product> spec = ProductSpecifications.build(criteria);
        return productRepository.findAll(spec, pageable);
    }

    @Transactional
    @CacheEvict(cacheNames = "productById", key = "#productId")
    public void evictProductCache(UUID productId) {
    }

    @Transactional
    @CacheEvict(cacheNames = "frontPageProducts", key = "'default'")
    public void evictFrontPageCache() {
    }
}
