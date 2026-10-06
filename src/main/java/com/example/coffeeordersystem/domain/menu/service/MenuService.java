package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private static final int POPULAR_MENU_LIMIT = 3;
    private static final int POPULAR_MENU_HOURS = 168;


    @Transactional(readOnly = true)
    public List<MenuResponse> getMenus() {
        return menuRepository.findAll(Sort.by("id")).stream()
                .map(MenuResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PopularMenuResponse> getPopularMenus() {
        LocalDateTime from = LocalDateTime.now().minusHours(POPULAR_MENU_HOURS);
        return menuRepository.findPopular(from, PageRequest.of(0, POPULAR_MENU_LIMIT)).stream()
                .map(PopularMenuResponse::from)
                .toList();
    }
}
