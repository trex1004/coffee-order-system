package com.example.coffeeordersystem.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull Long menuId,
        @Positive int quantity
) {
}
