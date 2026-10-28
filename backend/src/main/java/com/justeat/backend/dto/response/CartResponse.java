package com.justeat.backend.dto.response;

import com.justeat.backend.entity.Restaurant;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        Restaurant restaurant,
        List<CartItemResponse> items,
        BigDecimal subtotal
) {
    public record Restaurant(UUID id, String name) {}
}
