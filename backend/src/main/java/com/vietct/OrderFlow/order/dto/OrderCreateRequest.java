package com.vietct.OrderFlow.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        UUID userId, // placeholder, later link to real User
        @NotEmpty List<@Valid OrderItemRequest> items,
        @NotBlank String paymentMethod
) {
}
