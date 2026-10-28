package com.justeat.backend.service;

import com.justeat.backend.dto.request.AddCartItemRequest;
import com.justeat.backend.dto.request.UpdateCartItemRequest;
import com.justeat.backend.dto.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart(UUID customerId);

    CartResponse addItem(UUID customerId, AddCartItemRequest request);

    CartResponse updateItem(UUID customerId, UUID itemId, UpdateCartItemRequest request);

    CartResponse removeItem(UUID customerId, UUID itemId);

    void clearCart(UUID customerId);
}