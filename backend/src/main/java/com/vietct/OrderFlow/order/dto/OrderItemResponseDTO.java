package com.vietct.OrderFlow.order.dto;

import com.vietct.OrderFlow.order.domain.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponseDTO(
        UUID productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtOrder
) {
    public static OrderItemResponseDTO fromDomain(OrderItem item) {
        return new OrderItemResponseDTO(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtOrder()
        );
    }
}
