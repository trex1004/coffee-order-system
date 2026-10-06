package com.example.coffeeordersystem.domain.point.dto;

import com.example.coffeeordersystem.domain.user.entity.User;

public record ChargeResponse(
        Long userId,
        long chargedAmount,
        long balance
) {
    public static ChargeResponse of(User user, long chargedAmount) {
        return new ChargeResponse(
                user.getId(),
                chargedAmount,
                user.getPoint()
        );
    }
}
