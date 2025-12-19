package com.vietct.OrderFlow.order.service;

import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.dto.OrderCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    Order placeOrder(OrderCreateRequest request);
    Order getOrder(UUID id);
    Page<Order> getOrdersForUser(UUID userId, Pageable pageable);
}
