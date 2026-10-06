package com.example.coffeeordersystem.domain.point.repository;

import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import com.example.coffeeordersystem.domain.point.entity.PointType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    Optional<PointHistory> findByUserIdAndIdempotencyKeyAndType(Long userId, String idempotencyKey, PointType pointType);
}
