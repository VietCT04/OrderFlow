package com.vietct.OrderFlow.order.dto;

import com.vietct.OrderFlow.order.domain.Order;
import com.vietct.OrderFlow.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponseDTO(
        UUID id,
        UUID userId,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant updatedAt,
        List<OrderItemResponseDTO> items
) {
    public static OrderResponseDTO fromDomain(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getUserId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getItems().stream()
                        .map(OrderItemResponseDTO::fromDomain)
                        .toList()
        );
    }
}
