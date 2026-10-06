package com.example.coffeeordersystem.domain.order.facade;

import com.example.coffeeordersystem.domain.order.dto.OrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderResult;
import com.example.coffeeordersystem.domain.order.service.OrderService;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.LockTimeoutException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;


/**
 * 트랜잭션 단위의 재시도 경계를 관리한다.
 * Service의 트랜잭션이 롤백된 이후 새로운 트랜잭션으로
 * 재시도해야 하므로 재시도 로직은 트랜잭션 바깥에서 수행한다.
 */
@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;

    private static final long BACKOFF_MILLIS = 50L;
    private static final int MAX_ATTEMPTS = 3;

    public OrderResult order(OrderRequest request, String idempotencyKey) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return new OrderResult(orderService.place(request, idempotencyKey), true);
            } catch (CannotAcquireLockException e) {
                if (isLockTimeout(e)) {
                    throw e;                    // 재시도하지 않는다. 전역 핸들러가 503
                }
                sleepBackoff(attempt);
            }
        }
        throw new BusinessException(ErrorCode.ORDER_CONFLICT);
    }

    private boolean isLockTimeout(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof LockTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(BACKOFF_MILLIS * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.ORDER_CONFLICT);
        }
    }
}