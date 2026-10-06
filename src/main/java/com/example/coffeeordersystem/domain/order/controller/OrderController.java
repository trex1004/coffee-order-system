package com.example.coffeeordersystem.domain.order.controller;

import com.example.coffeeordersystem.domain.order.dto.OrderRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderResult;
import com.example.coffeeordersystem.domain.order.facade.OrderFacade;
import com.example.coffeeordersystem.global.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> order(
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 100) String idempotencyKey,
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResult result = orderFacade.order(request, idempotencyKey);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(ApiResponse.success(result.response()));
    }
}
