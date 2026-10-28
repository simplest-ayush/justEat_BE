package com.justeat.backend.dto.response;

import com.justeat.backend.entity.MenuItem;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        MenuItem menuItem,
        BigDecimal price,
        int quantity,
        BigDecimal lineTotal
) {
    public record MenuItem(UUID id, String name) {}
}
