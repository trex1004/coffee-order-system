package com.example.coffeeordersystem.domain.menu.entity;

import com.example.coffeeordersystem.global.entity.BaseEntity;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private int stock;

    private Menu(String name, long price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public static Menu of(String name, long price, int stock) {
        return new Menu(name, price, stock);
    }

    public MenuStatus getStatus() {
        return stock <= 0 ? MenuStatus.SOLD_OUT : MenuStatus.AVAILABLE;
    }

    public void deduct(int quantity) {
        if (stock < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }
}
