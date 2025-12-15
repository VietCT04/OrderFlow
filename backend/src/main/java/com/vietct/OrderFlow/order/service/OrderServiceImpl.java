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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

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
    @Transactional
    public Order placeOrder(OrderCreateRequest request) {
        Order order = new Order();
        order.setUserId(request.userId());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            UUID productId = itemRequest.productId();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new InventoryNotFoundException(productId));

            int requestedQty = itemRequest.quantity();
            int available = inventory.getAvailableQuantity();

            if (available < requestedQty) {
                throw new InsufficientStockException(productId);
            }

            inventory.setAvailableQuantity(available - requestedQty);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(requestedQty);
            orderItem.setPriceAtOrder(product.getPrice());

            order.addItem(orderItem);

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(requestedQty));
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);

        // Persist order first (so it has an ID for payment)
        Order savedOrder = orderRepository.save(order);

        // In Sprint 2 we treat payment as always-successful, synchronous
        paymentService.processPayment(savedOrder, total, request.paymentMethod());

        return savedOrder;
    }

    @Override
    public Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
