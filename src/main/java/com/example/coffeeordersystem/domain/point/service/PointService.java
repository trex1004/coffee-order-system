package com.example.coffeeordersystem.domain.point.service;


import com.example.coffeeordersystem.domain.point.dto.ChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.repository.PointHistoryRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChargeResponse charge(ChargeRequest request, String idempotencyKey) {
        User user = userRepository.findByIdForUpdate(request.userId()).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.charge(request.amount());
        pointHistoryRepository.save(
                PointHistory.charge(user.getId(), request.amount(), user.getPoint(), idempotencyKey)
        );
        log.debug("[충전] userId = {} amount = {} balance = {}", user.getId(), request.amount(), user.getPoint());
        return ChargeResponse.of(user, request.amount());
    }
}
