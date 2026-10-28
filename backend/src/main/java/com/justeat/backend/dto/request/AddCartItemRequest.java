package com.justeat.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull(message = "Menu item id is required")
        UUID menuItemId,

        @Positive(message = "Quantity must be greater than 0")
        int quantity
) {
}
