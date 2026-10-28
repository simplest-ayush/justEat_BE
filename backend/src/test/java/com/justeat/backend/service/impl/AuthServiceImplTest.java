package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.*;
import com.justeat.backend.dto.response.AuthResponse;
import com.justeat.backend.entity.*;
import com.justeat.backend.enums.Role;
import com.justeat.backend.exception.DuplicateEmailException;
import com.justeat.backend.exception.DuplicateUsernameException;
import com.justeat.backend.exception.InvalidTokenException;
import com.justeat.backend.repository.PasswordResetTokenRepository;
import com.justeat.backend.repository.RefreshTokenRepository;
import com.justeat.backend.repository.UserRepository;
import com.justeat.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .email("test@example.com")
                .password("hashed")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    void register_withNewUsername_savesUserAndReturnsAuthResponse() {
        RegisterRequest request = new RegisterRequest(
                "newuser", "new@example.com", "Pass@1234", "New User", "9876543210", Role.CUSTOMER);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Pass@1234")).thenReturn("encoded-hash");
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateOpaqueRefreshToken()).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse result = authService.register(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.expiresIn()).isEqualTo(900L);
        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_withDuplicateUsername_throwsDuplicateUsernameException() {
        RegisterRequest request = new RegisterRequest(
                "testuser", "another@example.com", "Pass@1234", null, null, Role.CUSTOMER);

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withDuplicateEmail_throwsDuplicateEmailException() {
        RegisterRequest request = new RegisterRequest(
                "uniqueuser", "test@example.com", "Pass@1234", null, null, Role.CUSTOMER);

        when(userRepository.existsByUsername("uniqueuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("testuser", "Pass@1234");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("access-token");
        when(jwtService.generateOpaqueRefreshToken()).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse result = authService.login(request);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.expiresIn()).isEqualTo(900L);
        verify(authenticationManager).authenticate(any());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_withValidToken_returnsNewAccessToken() {
        RefreshToken storedToken = RefreshToken.builder()
                .token("valid-refresh")
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        RefreshRequest request = new RefreshRequest("valid-refresh");
        when(refreshTokenRepository.findByToken("valid-refresh")).thenReturn(Optional.of(storedToken));
        when(jwtService.generateAccessToken(any(), any(), any())).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse result = authService.refresh(request);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("valid-refresh");
        assertThat(result.expiresIn()).isEqualTo(900L);
    }

    @Test
    void refresh_withRevokedToken_throwsInvalidTokenException() {
        RefreshToken revoked = RefreshToken.builder()
                .token("revoked-token")
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        RefreshRequest request = new RefreshRequest("revoked-token");
        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    void refresh_withExpiredToken_throwsInvalidTokenException() {
        RefreshToken expired = RefreshToken.builder()
                .token("expired-token")
                .user(testUser)
                .expiryDate(Instant.now().minusSeconds(60))
                .revoked(false)
                .build();

        RefreshRequest request = new RefreshRequest("expired-token");
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void refresh_withBlankToken_throwsInvalidTokenException() {
        RefreshRequest request = new RefreshRequest("  ");
        when(refreshTokenRepository.findByToken("  ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).findByToken("  ");
    }

    @Test
    void logout_withRecognizedToken_marksItRevoked() {
        RefreshToken storedToken = RefreshToken.builder()
                .token("logout-token").user(testUser).revoked(false).build();

        RefreshRequest request = new RefreshRequest("logout-token");
        when(refreshTokenRepository.findByToken("logout-token")).thenReturn(Optional.of(storedToken));

        authService.logout(request);

        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void logout_withNullToken_throwsNullPointerException() {
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(refreshTokenRepository);
    }
}