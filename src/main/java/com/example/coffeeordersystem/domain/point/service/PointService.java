package com.example.coffeeordersystem.domain.point.service;


import com.example.coffeeordersystem.domain.point.dto.ChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.entity.PointType;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChargeResponse charge(ChargeRequest request, String idempotencyKey) {
        // 1. 잠근다.
        User user = userRepository.findByIdForUpdate(request.userId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        // 2. 선조회
        Optional<PointHistory> history = pointHistoryRepository.findByUserIdAndIdempotencyKeyAndType(user.getId(), idempotencyKey, PointType.CHARGE);
        if (history.isPresent()) {
            return replay(history.get(), request);
        }

        user.charge(request.amount());
        pointHistoryRepository.save(
                PointHistory.charge(user.getId(), request.amount(), user.getPoint(), idempotencyKey)
        );
        log.info("[충전] userId = {} amount = {} balance = {}", user.getId(), request.amount(), user.getPoint());
        return ChargeResponse.of(user, request.amount());
    }

    private ChargeResponse replay(PointHistory history, ChargeRequest request) {
        if (history.getAmount() != request.amount()) {
            throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }
        return new ChargeResponse(history.getUserId(), history.getAmount(), history.getBalanceAfter());
    }
}
