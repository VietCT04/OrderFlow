package com.vietct.OrderFlow.order.controller;

import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.dto.OrderCreateRequest;
import com.vietct.OrderFlow.order.dto.OrderResponseDTO;
import com.vietct.OrderFlow.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponseDTO placeOrder(@Valid @RequestBody OrderCreateRequest request) {
        Order order = orderService.placeOrder(request);
        return OrderResponseDTO.fromDomain(order);
    }

    @GetMapping("/{id}")
    public OrderResponseDTO getOrder(@PathVariable UUID id) {
        Order order = orderService.getOrder(id);
        return OrderResponseDTO.fromDomain(order);
    }

    @GetMapping
    public Page<OrderResponseDTO> getOrdersForUser(
            @RequestParam UUID userId,
            Pageable pageable
    ) {
        Page<Order> page = orderService.getOrdersForUser(userId, pageable);
        return page.map(OrderResponseDTO::fromDomain);
    }
}
