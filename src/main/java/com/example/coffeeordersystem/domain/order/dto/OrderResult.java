package com.example.coffeeordersystem.domain.order.dto;

public record OrderResult(
        OrderResponse response,
        boolean created
) {
}
