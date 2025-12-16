package com.vietct.OrderFlow.order.bootstrap;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.repository.ProductRepository;
import com.vietct.OrderFlow.inventory.domain.Inventory;
import com.vietct.OrderFlow.inventory.repository.InventoryRepository;
import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.dto.OrderCreateRequest;
import com.vietct.OrderFlow.order.dto.OrderItemRequest;
import com.vietct.OrderFlow.order.dto.OrderResponseDTO;
import com.vietct.OrderFlow.order.service.OrderService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
@Profile("dev")
public class OrderConcurrencyDemoRunner {

    private static final Logger log = LoggerFactory.getLogger(OrderConcurrencyDemoRunner.class);

    private static final boolean ENABLED = true;

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderService orderService;

    public OrderConcurrencyDemoRunner(ProductRepository productRepository,
                                      InventoryRepository inventoryRepository,
                                      OrderService orderService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderService = orderService;
    }

    @PostConstruct
    public void runDemoIfEnabled() {
        if (!ENABLED) {
            return;
        }

        log.info("=== Starting OrderConcurrencyDemoRunner ===");

        Product product = productRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDemoProduct);

        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElseGet(() -> createInventory(product));

        inventory.setAvailableQuantity(1);
        inventoryRepository.save(inventory);

        log.info("Demo product id={} price={} stock={}",
                product.getId(), product.getPrice(), inventory.getAvailableQuantity());

        // 3) Build request (same product, qty=1)
        UUID demoUserId = UUID.randomUUID();
        OrderItemRequest itemRequest = new OrderItemRequest(product.getId(), 1);
        OrderCreateRequest orderRequest = new OrderCreateRequest(
                demoUserId,
                List.of(itemRequest),
                "MOCK_CARD"
        );

        var executor = Executors.newFixedThreadPool(2, new NamedThreadFactory("order-demo-"));

        CountDownLatch latch = new CountDownLatch(2);

        Runnable task = () -> {
            try {
                log.info("Thread {} placing order...", Thread.currentThread().getName());
                Order order = orderService.placeOrder(orderRequest);
                OrderResponseDTO dto = OrderResponseDTO.fromDomain(order);
                log.info("Thread {} SUCCESS: orderId={} status={} total={}",
                        Thread.currentThread().getName(),
                        dto.id(), dto.status(), dto.totalAmount());
            } catch (Exception ex) {
                log.warn("Thread {} FAILED: {}", Thread.currentThread().getName(), ex.getMessage());
            } finally {
                latch.countDown();
            }
        };

        executor.submit(task);
        executor.submit(task);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executor.shutdown();

        Inventory after = inventoryRepository.findByProductId(product.getId())
                .orElseThrow();

        log.info("After concurrency demo: productId={} availableQuantity={}",
                product.getId(), after.getAvailableQuantity());

        log.info("=== Finished OrderConcurrencyDemoRunner ===");
    }

    private Product createDemoProduct() {
        Product p = new Product();
        p.setName("Demo Product for Concurrency Test");
        p.setDescription("Demo");
        p.setPrice(new BigDecimal("10.00"));
        return productRepository.save(p);
    }

    private Inventory createInventory(Product product) {
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setAvailableQuantity(1);
        return inventoryRepository.save(inv);
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private int counter = 0;

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, prefix + counter++);
        }
    }
}
