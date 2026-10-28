package com.justeat.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FavoriteRestaurantResponse(
        Restaurant restaurant,
        Instant addedAt
) {
    public record Restaurant(UUID id, String name) {}
}
