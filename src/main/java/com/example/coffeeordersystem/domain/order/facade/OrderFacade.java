package com.example.coffeeordersystem.domain.order.facade;

import com.example.coffeeordersystem.domain.order.dto.OrderItemRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderResult;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.order.service.OrderService;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.LockTimeoutException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 주문 처리의 트랜잭션 경계 밖 흐름을 조율한다.
 * OrderService의 트랜잭션이 롤백된 이후에는
 * 새로운 트랜잭션으로 재시도해야 하므로 재시도 로직은 여기서 수행한다.
 * 실제 주문 처리와 데이터 변경은 OrderService가 하나의 트랜잭션으로 수행한다.
 */
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderResult order(OrderRequest request, String key) {
        validateNoDuplicateMenu(request.items());
        return orderRepository.findByUserIdAndIdempotencyKey(request.userId(), key)
                .map(found -> new OrderResult(replay(found, request), false))
                .orElseGet(() -> place(request, key));
    }

    private OrderResult place(OrderRequest request, String key) {
        try {
            return new OrderResult(orderService.place(request, key), true);
        } catch (DataIntegrityViolationException e) {
            return orderRepository.findByUserIdAndIdempotencyKey(request.userId(), key)
                    .map(found -> new OrderResult(replay(found, request), false))
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_CONFLICT));
        }
    }

    // 저장된 주문으로 응답을 다시 만든다
    private OrderResponse replay(Order found, OrderRequest request) {
        if (!hasSameItems(found, request.items())) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }
        return OrderResponse.of(found);
    }

    // 요청 항목이 저장된 주문과 같은지 본다
    private boolean hasSameItems(Order found, List<OrderItemRequest> requestItems) {
        if (found.getItems().size() != requestItems.size()) {
            return false;
        }
        Map<Long, Integer> saved = found.getItems().stream()
                .collect(Collectors.toMap(item -> item.getMenu().getId(), OrderItem::getQuantity));
        return requestItems.stream()
                .allMatch(r -> Objects.equals(saved.get(r.menuId()), r.quantity()));
    }

    // 같은 메뉴가 두 항목으로 나뉘었는지 검사한다
    private void validateNoDuplicateMenu(List<OrderItemRequest> items) {
        long distinct = items.stream().map(OrderItemRequest::menuId).distinct().count();
        if (distinct != items.size()) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_ITEMS);
        }
    }
}