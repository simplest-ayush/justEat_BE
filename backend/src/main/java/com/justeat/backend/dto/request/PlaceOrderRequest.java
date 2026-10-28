package com.justeat.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PlaceOrderRequest(
        @NotBlank(message = "Delivery address cannot be blank")
        @Size(max = 300, message = "Delivery address must not exceed 300 characters")
        String deliveryAddress
) {
}