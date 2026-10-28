package com.justeat.backend.service;

import com.justeat.backend.dto.request.PreferenceUpdateRequest;
import com.justeat.backend.dto.response.PreferenceResponse;

import java.util.UUID;

public interface PreferenceService {

    PreferenceResponse getPreferences(UUID customerId);

    PreferenceResponse updatePreferences(UUID customerId, PreferenceUpdateRequest request);

    void addFavorite(UUID customerId, UUID restaurantId);

    void removeFavorite(UUID customerId, UUID restaurantId);
}