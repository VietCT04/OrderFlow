package com.vietct.OrderFlow.order.service;

import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.dto.OrderCreateRequest;

import java.util.UUID;

public interface OrderService {

    Order placeOrder(OrderCreateRequest request);

    Order getOrder(UUID id);
}
