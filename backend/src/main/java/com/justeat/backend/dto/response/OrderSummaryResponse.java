package com.justeat.backend.dto.response;

import com.justeat.backend.entity.Restaurant;
import com.justeat.backend.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        String orderNumber,
        Restaurant restaurant,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant placedAt
) {
    public record Restaurant(UUID id, String name) {}
}
