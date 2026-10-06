package com.example.coffeeordersystem.domain.point.repository;

import com.example.coffeeordersystem.domain.point.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
