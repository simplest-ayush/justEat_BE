package com.justeat.backend.service;

import com.justeat.backend.dto.request.RestaurantRequest;
import com.justeat.backend.dto.response.RestaurantResponse;
import com.justeat.backend.dto.response.RestaurantSummaryResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RestaurantService {

    RestaurantResponse create(UUID ownerId, RestaurantRequest request);

    RestaurantResponse update(UUID ownerId, UUID restaurantId, RestaurantRequest request);

    void delete(UUID ownerId, UUID restaurantId);

    RestaurantResponse getById(UUID restaurantId);

    List<RestaurantResponse> getMyRestaurants(UUID ownerId);

    Page<RestaurantSummaryResponse> browse(
            String query, String cuisine, String city, Pageable pageable);
}
