package com.justeat.backend.service;

import com.justeat.backend.dto.request.MarkSpecialRequest;
import com.justeat.backend.dto.request.MenuItemRequest;
import com.justeat.backend.dto.response.MenuItemResponse;

import java.util.List;
import java.util.UUID;

public interface MenuItemService {

    MenuItemResponse create(UUID ownerId, UUID restaurantId, MenuItemRequest request);

    MenuItemResponse update(UUID ownerId, UUID restaurantId, UUID itemId, MenuItemRequest request);

    void delete(UUID ownerId, UUID restaurantId, UUID itemId);

    MenuItemResponse markSpecial(UUID ownerId, UUID restaurantId, UUID itemId, MarkSpecialRequest request);

    List<MenuItemResponse> getByRestaurant(UUID restaurantId, String category);

    List<MenuItemResponse> getMostlyOrdered(UUID restaurantId);
}
