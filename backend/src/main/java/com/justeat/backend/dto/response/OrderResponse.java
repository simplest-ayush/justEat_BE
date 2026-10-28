package com.justeat.backend.dto.response;

import com.justeat.backend.entity.Restaurant;
import com.justeat.backend.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        Restaurant restaurant,
        OrderStatus status,
        BigDecimal totalAmount,
        String deliveryAddress,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory,
        Instant placedAt
) {
    public record Restaurant(UUID id, String name) {}
}
