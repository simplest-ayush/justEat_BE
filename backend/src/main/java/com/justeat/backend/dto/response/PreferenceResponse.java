package com.justeat.backend.dto.response;

import com.justeat.backend.enums.DietaryRestriction;

import java.util.List;
import java.util.Set;

public record PreferenceResponse(
        Set<String> preferredCuisines,
        Set<DietaryRestriction> dietaryRestrictions,
        List<FavoriteRestaurantResponse> favoriteRestaurants
) {
}
