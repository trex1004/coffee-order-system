package com.example.coffeeordersystem.domain.order.entity;

import com.example.coffeeordersystem.domain.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_amount", nullable = false)
    private long lineAmount;

    private OrderItem(Menu menu, int quantity, long lineAmount) {
        this.menu = menu;
        this.quantity = quantity;
        this.lineAmount = lineAmount;
    }

    public static OrderItem of(Menu menu, int quantity) {
        return new OrderItem(menu, quantity, menu.getPrice() * quantity);
    }

    void assignTo(Order order) {
        this.order = order;
    }
}
