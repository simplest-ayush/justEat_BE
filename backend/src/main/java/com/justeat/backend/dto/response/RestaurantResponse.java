package com.justeat.backend.dto.response;

import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        Owner owner,
        String name,
        String cuisineType,
        String description,
        String address,
        String city,
        String phone,
        boolean active
) {
    public record Owner(UUID id, String name){}
}
