package com.justeat.backend.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        UUID restaurantId,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean active,
        boolean special,
        Instant specialUntil,
        boolean mostlyOrdered
) {
}
