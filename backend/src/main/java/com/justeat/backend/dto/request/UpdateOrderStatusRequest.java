package com.justeat.backend.dto.request;

import com.justeat.backend.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "New status is required")
        OrderStatus newStatus
) {
}
