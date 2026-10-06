package com.example.coffeeordersystem.domain.user.entity;

import com.example.coffeeordersystem.global.entity.BaseEntity;
import com.example.coffeeordersystem.global.exception.BusinessException;
import com.example.coffeeordersystem.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private long point;

    private User(String email, String name) {
        this.email = email;
        this.name = name;
        this.point = 0L;
    }

    public static User of(String email, String name) {
        return new User(email, name);
    }

    public void charge(long amount) {
        this.point += amount;
    }

    public void use(long amount) {
        if (point < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        this.point -= amount;
    }
}