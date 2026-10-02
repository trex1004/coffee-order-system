package com.example.coffeeordersystem.domain.pointhistory.repository;

import com.example.coffeeordersystem.domain.pointhistory.entity.PointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
}
