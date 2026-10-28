package com.justeat.backend.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(
        @Positive(message = "Quantity must be greater than 0")
        int quantity
) {
}
