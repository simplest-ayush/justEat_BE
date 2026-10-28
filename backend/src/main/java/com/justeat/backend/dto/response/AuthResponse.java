package com.justeat.backend.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
