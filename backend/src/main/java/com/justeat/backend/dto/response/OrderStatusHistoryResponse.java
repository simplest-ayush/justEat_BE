package com.justeat.backend.dto.response;

import com.justeat.backend.enums.OrderStatus;

import java.time.Instant;

public record OrderStatusHistoryResponse(
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String changedByUsername,
        Instant changedAt
) {
}
