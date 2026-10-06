package com.example.coffeeordersystem.domain.order.dto;

import com.example.coffeeordersystem.domain.order.entity.Order;

import java.util.List;

public record OrderResponse(
        Long orderId,
        List<OrderItemResponse> items,
        long totalAmount,
        long balance
) {
    public static OrderResponse of(Order order) {

        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();
        return new OrderResponse(order.getId(), items, order.getTotalAmount(), order.getBalanceAfter());
    }
}