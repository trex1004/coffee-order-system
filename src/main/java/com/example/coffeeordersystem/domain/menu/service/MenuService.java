package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.domain.menu.dto.MenuResponse;
import com.example.coffeeordersystem.domain.menu.dto.PopularMenuResponse;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional(propagation = Propagation.MANDATORY)
    public Menu decreaseStock(Long menuId, int quantity) {
        Menu menu = menuRepository.findByIdForUpdate(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        menu.deduct(quantity);
        log.info("[재고 차감] menuId={} quantity={} stock={}", menuId, quantity, menu.getStock());
        return menu;
    }
}
