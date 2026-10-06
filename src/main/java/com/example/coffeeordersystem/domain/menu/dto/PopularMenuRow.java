package com.example.coffeeordersystem.domain.menu.dto;

import com.example.coffeeordersystem.domain.menu.entity.Menu;

public record PopularMenuRow(
        Menu menu,
        long orderCount

) {
}
