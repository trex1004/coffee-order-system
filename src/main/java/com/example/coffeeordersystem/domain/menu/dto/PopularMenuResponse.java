package com.example.coffeeordersystem.domain.menu.dto;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;

public record PopularMenuResponse(
        Long menuId,
        String name,
        long orderCount,
        MenuStatus status
) {
    public static PopularMenuResponse from(PopularMenuRow row) {
        Menu menu = row.menu();
        return new PopularMenuResponse(
                menu.getId(),
                menu.getName(),
                row.orderCount(),
                menu.getStatus()

        );
    }
}
