package com.justeat.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "Refresh token cannot be blank")
        String refreshToken
) {
}