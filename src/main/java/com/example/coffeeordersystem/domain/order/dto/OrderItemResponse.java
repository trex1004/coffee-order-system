package com.example.coffeeordersystem.domain.order.dto;

import com.example.coffeeordersystem.domain.order.entity.OrderItem;

public record OrderItemResponse(
        Long menuId,
        int quantity,
        long lineAmount) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getMenu().getId(),
                item.getQuantity(),
                item.getLineAmount());
    }
}