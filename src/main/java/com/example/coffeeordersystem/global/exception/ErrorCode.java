package com.example.coffeeordersystem.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 400
    MISSING_IDEMPOTENCY_KEY(HttpStatus.BAD_REQUEST, "Idempotency-Key 헤더가 필요합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "입력값을 확인해주세요."),
    INVALID_ORDER_ITEMS(HttpStatus.BAD_REQUEST, "같은 메뉴는 한 항목에 수량으로 합쳐야 합니다."),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴입니다."),
    URL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경로입니다."),

    // 405 , 415
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),

    // 409
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "포인트가 부족합니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "재고가 부족합니다."),
    ORDER_CONFLICT(HttpStatus.CONFLICT, "주문 처리에 실패했습니다. 다시 시도해 주세요."),
    IDEMPOTENCY_KEY_CONFLICT(HttpStatus.CONFLICT, "이미 사용한 요청 키입니다."),

    // 500 / 503
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    LOCK_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "요청이 많아 처리하지 못했습니다. 다시 시도해 주세요.");

    private final HttpStatus status;
    private final String message;
}