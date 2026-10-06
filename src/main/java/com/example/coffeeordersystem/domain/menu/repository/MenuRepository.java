package com.example.coffeeordersystem.domain.menu.repository;

import com.example.coffeeordersystem.domain.menu.dto.PopularMenuRow;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {


    @Query("""
            SELECT new com.example.coffeeordersystem.domain.menu.dto.PopularMenuRow(m, COUNT(oi.id))
            FROM OrderItem oi
            JOIN oi.menu m
            JOIN oi.order o
            WHERE o.createdAt >= :from
            GROUP BY m
            ORDER BY COUNT(oi.id) DESC, m.id ASC
            """)
    List<PopularMenuRow> findPopular(@Param("from") LocalDateTime from, Pageable pageable);
}

