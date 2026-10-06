package com.example.coffeeordersystem.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull Long userId,
        @NotEmpty @Valid List<OrderItemRequest> items
) {
}
