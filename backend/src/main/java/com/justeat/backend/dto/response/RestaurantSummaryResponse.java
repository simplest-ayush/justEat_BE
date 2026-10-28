package com.justeat.backend.dto.response;

import java.util.UUID;

public record RestaurantSummaryResponse(
        UUID id,
        String name,
        String cuisineType,
        String city
) {
}
