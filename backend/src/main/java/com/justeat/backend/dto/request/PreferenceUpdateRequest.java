package com.justeat.backend.dto.request;

import com.justeat.backend.enums.DietaryRestriction;

import java.util.Set;

public record PreferenceUpdateRequest(
        Set<String> preferredCuisines,
        Set<DietaryRestriction> dietaryRestrictions
) {
}
