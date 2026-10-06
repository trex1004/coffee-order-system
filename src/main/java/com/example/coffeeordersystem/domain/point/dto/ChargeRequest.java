package com.example.coffeeordersystem.domain.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChargeRequest(
        @NotNull Long userId,
        @Positive long amount
) {
}
