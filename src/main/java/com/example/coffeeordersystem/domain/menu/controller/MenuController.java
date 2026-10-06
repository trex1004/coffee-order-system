package com.example.coffeeordersystem.domain.menu.controller;

import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.service.MenuService;
import com.example.coffeeordersystem.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<List<MenuResponse>> getMenu() {
        return ApiResponse.success(menuService.getMenus());
    }

    @GetMapping("/popular")
    public ApiResponse<List<PopularMenuResponse>> getPopularMenus() {
        return ApiResponse.success(menuService.getPopularMenus());
    }
}
