package com.example.coffeeordersystem.domain.point.entity;

import com.example.coffeeordersystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "point_histories",
        indexes = @Index(name = "idx_point_histories_user",
                columnList = "user_id"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_point_histories_user_idempotency_key",
                columnNames = {"user_id", "idempotency_key"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id", nullable = false)
        private Long userId;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 10)
        private PointType type;

        @Column(nullable = false)
        private long amount;

        @Column(name = "balance_after", nullable = false)
        private long balanceAfter;

        @Column(name = "idempotency_key", length = 100)
        private String idempotencyKey;

        private PointHistory(Long userId, PointType type, long amount, long balanceAfter, String idempotencyKey) {
                this.userId = userId;
                this.type = type;
                this.amount = amount;
                this.balanceAfter = balanceAfter;
                this.idempotencyKey = idempotencyKey;
        }

        public static PointHistory charge(Long userId, long amount, long balanceAfter, String idempotencyKey) {
                return new PointHistory(userId, PointType.CHARGE, amount, balanceAfter, idempotencyKey);
        }

        public static PointHistory use(Long userId, long amount, long balanceAfter) {
                return new PointHistory(userId, PointType.USE, -amount, balanceAfter, null);
        }
}
