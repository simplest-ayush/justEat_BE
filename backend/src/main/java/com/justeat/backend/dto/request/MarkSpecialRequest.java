package com.justeat.backend.dto.request;

import jakarta.validation.constraints.Future;

import java.time.Instant;

public record MarkSpecialRequest(
        boolean special,
        @Future(message = "specialUntil must be a future date")
        Instant specialUntil
) {
}