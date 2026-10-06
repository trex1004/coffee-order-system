package com.example.coffeeordersystem.domain.point.controller;

import com.example.coffeeordersystem.domain.point.dto.ChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.ChargeResponse;
import com.example.coffeeordersystem.domain.point.service.PointService;
import com.example.coffeeordersystem.global.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public ApiResponse<ChargeResponse> charge(@RequestHeader("Idempotency-Key")
                                              @NotBlank @Size(max = 100) String idempotencyKey,
                                              @Valid @RequestBody ChargeRequest request) {
        return ApiResponse.success(pointService.charge(request, idempotencyKey));
    }
}
