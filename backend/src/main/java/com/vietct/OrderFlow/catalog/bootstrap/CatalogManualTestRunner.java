package com.vietct.OrderFlow.catalog.bootstrap;

import com.vietct.OrderFlow.catalog.domain.Category;
import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.repository.CategoryRepository;
import com.vietct.OrderFlow.catalog.repository.ProductRepository;
import com.vietct.OrderFlow.catalog.service.CatalogService;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Component
@Profile("dev") // runs only in dev profile
public class CatalogManualTestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogManualTestRunner.class);

    private final CatalogService catalogService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CatalogManualTestRunner(CatalogService catalogService,
                                   CategoryRepository categoryRepository,
                                   ProductRepository productRepository) {
        this.catalogService = catalogService;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        log.info("=== CatalogManualTestRunner starting ===");

        seedSampleDataIfEmpty();

        // 1) Retrieve a product by ID using the service
        productRepository.findAll(PageRequest.of(0, 1))
                .getContent()
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        product -> testGetProductById(product.getId()),
                        () -> log.warn("No products found after seeding; cannot test getProductById")
                );

        // 2) Retrieve a paginated, sorted list of products
        testGetProductsPage();

        log.info("=== CatalogManualTestRunner finished ===");
    }

    private void seedSampleDataIfEmpty() {
        long productCount = productRepository.count();
        if (productCount > 0) {
            log.info("Skipping seeding: {} products already in database", productCount);
            return;
        }

        log.info("No products found; seeding sample categories and products for manual testing");

        Category electronics = new Category(
                "Electronics",
                "electronics",
                "Devices, gadgets, and accessories"
        );
        electronics = categoryRepository.save(electronics);

        Category furniture = new Category(
                "Furniture",
                "furniture",
                "Chairs, desks, and more"
        );
        furniture = categoryRepository.save(furniture);

        Product p1 = new Product(
                "Wireless Noise-Cancelling Headphones",
                "Over-ear wireless headphones with active noise cancellation.",
                new BigDecimal("199.90"),
                50,
                null,
                electronics
        );

        Product p2 = new Product(
                "Mechanical Keyboard (75%)",
                "Compact mechanical keyboard with hot-swappable switches.",
                new BigDecimal("129.50"),
                30,
                null,
                electronics
        );

        Product p3 = new Product(
                "Ergonomic Office Chair",
                "Adjustable ergonomic chair suitable for long working hours.",
                new BigDecimal("329.00"),
                10,
                null,
                furniture
        );

        productRepository.saveAll(List.of(p1, p2, p3));

        log.info("Seeded sample data: {} categories, {} products",
                categoryRepository.count(), productRepository.count());
    }

    private void testGetProductById(UUID productId) {
        log.info("Testing CatalogService.getProductById for id={}", productId);

        Product product = catalogService.getProductById(productId);

        log.info("getProductById result: id={}, name='{}', price={}, category='{}'",
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory().getName());
    }

    private void testGetProductsPage() {
        log.info("Testing CatalogService.getProducts with pagination");

        PageRequest pageRequest = PageRequest.of(
                0, // first page (0-based)
                2, // page size
                Sort.by(Sort.Direction.ASC, "price")
        );

        // Passing null categoryId to get all products
        Page<Product> page = catalogService.getProducts(null, pageRequest);

        log.info("getProducts page0 size2: totalElements={}, totalPages={}, numberOfElements={}",
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumberOfElements());

        page.getContent().forEach(product ->
                log.info("  product: id={}, name='{}', price={}",
                        product.getId(),
                        product.getName(),
                        product.getPrice())
        );
    }
}
