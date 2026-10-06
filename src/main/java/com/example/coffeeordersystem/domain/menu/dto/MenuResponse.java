package com.example.coffeeordersystem.domain.menu.dto;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.entity.MenuStatus;

public record MenuResponse(
        Long menuId,
        String name,
        long price,
        MenuStatus status
) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getStatus()
        );
    }
}
