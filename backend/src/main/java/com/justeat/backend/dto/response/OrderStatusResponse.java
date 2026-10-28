package com.justeat.backend.dto.response;

import com.justeat.backend.enums.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusResponse(
        UUID orderId,
        OrderStatus status,
        Instant lastUpdatedAt
) {
}
