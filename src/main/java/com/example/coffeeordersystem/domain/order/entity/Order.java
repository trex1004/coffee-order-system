package com.example.coffeeordersystem.domain.order.entity;


import com.example.coffeeordersystem.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_orders_user_idempotency_key",
                columnNames = {"user_id", "idempotency_key"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "total_amount", nullable = false)
    private long totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    private Order(Long userId, String idempotencyKey) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.totalAmount = 0L;
    }

    public static Order of(Long userId, String idempotencyKey) {
        return new Order(userId, idempotencyKey);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.assignTo(this);
        this.totalAmount += item.getLineAmount();
    }

    public void recordBalance(long balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
}
