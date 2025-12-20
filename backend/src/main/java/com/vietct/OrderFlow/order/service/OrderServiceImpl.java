package com.vietct.OrderFlow.order.service;

import com.vietct.OrderFlow.catalog.domain.Product;
import com.vietct.OrderFlow.catalog.exception.ProductNotFoundException;
import com.vietct.OrderFlow.catalog.repository.ProductRepository;
import com.vietct.OrderFlow.inventory.domain.Inventory;
import com.vietct.OrderFlow.inventory.exception.InsufficientStockException;
import com.vietct.OrderFlow.inventory.exception.InventoryNotFoundException;
import com.vietct.OrderFlow.inventory.repository.InventoryRepository;
import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.domain.OrderItem;
import com.vietct.OrderFlow.order.domain.OrderStatus;
import com.vietct.OrderFlow.order.dto.OrderCreateRequest;
import com.vietct.OrderFlow.order.dto.OrderItemRequest;
import com.vietct.OrderFlow.order.exception.OrderNotFoundException;
import com.vietct.OrderFlow.order.repository.OrderRepository;
import com.vietct.OrderFlow.payment.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentService paymentService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository,
                            PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentService = paymentService;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Order placeOrder(OrderCreateRequest request) {
        try {
            return doPlaceOrderInternal(request);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
            throw new InsufficientStockException("Failed to place order due to concurrent stock updates");
        }
    }

    private Order doPlaceOrderInternal(OrderCreateRequest request) {
        List<UUID> productIds = request.items().stream()
                .map(OrderItemRequest::productId)
                .toList();

        var productsById = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), Function.identity()));

        Order order = new Order();
        order.setUserId(request.userId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            UUID productId = itemRequest.productId();

            Product product = productsById.get(productId);
            if (product == null) {
                throw new ProductNotFoundException(productId);
            }

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new InventoryNotFoundException(productId));

            int requestedQty = itemRequest.quantity();
            if (requestedQty <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for product: " + productId);
            }

            int available = inventory.getAvailableQuantity();
            if (available < requestedQty) {
                throw new InsufficientStockException(productId);
            }

            inventory.setAvailableQuantity(available - requestedQty);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQty);
            orderItem.setPriceAtOrder(product.getPrice());

            items.add(orderItem);

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(requestedQty));
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        paymentService.processPayment(savedOrder, total, request.paymentMethod());

        return savedOrder;
    }

    @Override
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersForUser(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
}
