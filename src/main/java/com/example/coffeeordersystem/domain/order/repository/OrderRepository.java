package com.example.coffeeordersystem.domain.order.repository;

import com.example.coffeeordersystem.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {

    @Query("""
        SELECT o FROM Order o
        JOIN FETCH o.items i
        JOIN FETCH i.menu
        WHERE o.userId = :userId AND o.idempotencyKey = :key
        """)
    Optional<Order> findByUserIdAndIdempotencyKey(@Param("userId") Long userId, @Param("key") String key);
}
