package com.example.coffeeordersystem.global.dto;

import com.example.coffeeordersystem.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data) {

    private static final String SUCCESS = "SUCCESS";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS, null, data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.name(), errorCode.getMessage(), null);
    }
}