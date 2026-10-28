package com.justeat.backend.security;

import com.justeat.backend.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Injecting @Value-backed fields directly since this is a plain unit test,
        // not a @SpringBootTest - no application context loaded here on purpose.
        ReflectionTestUtils.setField(jwtService, "secret",
                "test-secret-key-must-be-long-enough-for-hmac-sha256-signing-1234567890");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", 900000L);
    }

    @Test
    void generateAccessToken_thenExtractUserId_roundTripsCorrectly() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "testuser", Role.CUSTOMER);

        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void generateAccessToken_thenExtractRole_roundTripsCorrectly() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, "testuser", Role.RESTAURANT_OWNER);

        assertThat(jwtService.extractRole(token)).isEqualTo(Role.RESTAURANT_OWNER);
    }

    @Test
    void isTokenValid_forFreshlyGeneratedToken_returnsTrue() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "testuser", Role.CUSTOMER);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_forTamperedToken_returnsFalse() {
        String token = jwtService.generateAccessToken(UUID.randomUUID(), "testuser", Role.CUSTOMER);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}