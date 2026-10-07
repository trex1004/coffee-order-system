package com.example.coffeeordersystem.domain.order.service;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.service.MenuService;
import com.example.coffeeordersystem.domain.order.dto.OrderItemRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.point.service.PointService;
import com.example.coffeeordersystem.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final PointService pointService;
    private final MenuService menuService;


    // 재고와 포인트를 차감하고 주문을 만든다
    @Transactional
    public OrderResponse place(OrderRequest request, String idempotencyKey) {
        User user = pointService.lockUser(request.userId());

        Order order = Order.of(request.userId(), idempotencyKey);

        for (OrderItemRequest item : sortedByMenuId(request.items())) {
            Menu menu = menuService.decreaseStock(item.menuId(), item.quantity());
            order.addItem(OrderItem.of(menu, item.quantity()));
        }

        long balance = pointService.use(user, order.getTotalAmount());
        order.recordBalance(balance);
        orderRepository.save(order);
        return OrderResponse.of(order);
    }

    // 메뉴 ID 오름차순으로 정렬한다
    private List<OrderItemRequest> sortedByMenuId(List<OrderItemRequest> items) {
        return items.stream()
                .sorted(Comparator.comparing(OrderItemRequest::menuId))
                .toList();
    }
}
