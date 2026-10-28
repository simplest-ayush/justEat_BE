package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.*;
import com.justeat.backend.dto.response.AuthResponse;
import com.justeat.backend.entity.PasswordResetToken;
import com.justeat.backend.entity.RefreshToken;
import com.justeat.backend.entity.User;
import com.justeat.backend.exception.DuplicateEmailException;
import com.justeat.backend.exception.DuplicateUsernameException;
import com.justeat.backend.exception.InvalidTokenException;
import com.justeat.backend.repository.PasswordResetTokenRepository;
import com.justeat.backend.repository.RefreshTokenRepository;
import com.justeat.backend.repository.UserRepository;
import com.justeat.backend.security.JwtService;
import com.justeat.backend.service.AuthService;
import com.justeat.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final long RESET_TOKEN_EXPIRATION_MS = 900_000L; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 604_800_000L; // 7 days

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username={}", request.username());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.error("Username not found");
                    return new InvalidTokenException("User not found: " + request.username());
                });

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshTokenValue = jwtService.generateOpaqueRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Login successful for username={}", request.username());
        return new AuthResponse(accessToken, refreshTokenValue, jwtService.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username={}", request.username());

        if (userRepository.existsByUsername(request.username())) {
            log.error("Duplicate username on register: username={}", request.username());
            throw new DuplicateUsernameException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            log.error("Duplicate email on register: email={}", request.email());
            throw new DuplicateEmailException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(request.role())
                .enabled(true)
                .build();
        userRepository.save(user);

        log.info("Registered new {} account: username={}", request.role(), request.username());

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshTokenValue = jwtService.generateOpaqueRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_MS))
                .build();
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(accessToken, refreshTokenValue, jwtService.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> {
                    log.error("Refresh token not recognized");
                    return new InvalidTokenException("Refresh token not recognized");
                });

        if (storedToken.isRevoked()) {
            log.error("Attempted refresh with revoked token");
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            log.error("Attempted refresh with expired token");
            throw new InvalidTokenException("Refresh token has expired");
        }

        User user = storedToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());

        log.info("Access token refreshed for username={}", user.getUsername());
        return new AuthResponse(newAccessToken, storedToken.getToken(), jwtService.getAccessTokenExpirationSeconds());
    }

    @Override
    @Transactional
    public void logout(RefreshRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> {
                    log.error("Refresh token not recognized on logout attempt");
                    return new InvalidTokenException("Refresh token not recognized");
                });

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        log.info("Logout successful, refresh token revoked");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request){
        log.info("Password reset requested for email={}", request.email());

        userRepository.findByEmail(request.email()).ifPresent(user->{
            String rawToken= UUID.randomUUID().toString();
            String tokenHash=hashToken(rawToken);

            PasswordResetToken resetToken=PasswordResetToken.builder()
                    .tokenHash(tokenHash)
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(RESET_TOKEN_EXPIRATION_MS))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request){
        String tokenHash=hashToken(request.token());

        PasswordResetToken resetToken=passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(()->{
                    log.warn("Password reset attempted with unrecognized token");
                    return new InvalidTokenException("Reset token is invalid");
                });

        if(resetToken.isUsed()){
            log.warn("Password reset attempted with already-used token");
            throw new InvalidTokenException("Reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Password reset attempted with expired token");
            throw new InvalidTokenException("Reset token has expired");
        }

        User user=resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        log.info("DEBUG - about to save, new hash={}", user.getPassword());  // ADD THIS
        userRepository.save(user);
        log.info("DEBUG - user after save, id={}, hash={}", user.getId(), user.getPassword());

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for userId={}", user.getId());
    }

    private String hashToken(String rawToken){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

}
