package com.justeat.backend.controller;

import com.justeat.backend.dto.request.*;
import com.justeat.backend.dto.response.AuthResponse;
import com.justeat.backend.dto.response.MessageResponse;
import com.justeat.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auth", description = "Login, Register, token refresh, and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Log in and receive an access + refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        log.info("Received login request {}", request);
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new Customer or Restaurant Owner account",
            description = "Publicly accessible. The role in the request determines the account type.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - username={}", request.username());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange a valid refresh token for a new access token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Access token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or revoked"),
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoke a refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successful, refresh token revoked"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Refresh token not recognized"),
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        log.info("POST /api/v1/auth/logout");
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset token",
            description = "Always returns 202 regardless of whether the email exists, to prevent account enumeration.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted, reset link sent if email exists"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
    })
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/v1/auth/forgot-password");
        authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new MessageResponse("If that email exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using a valid reset token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Reset token is invalid, expired, or already used"),
    })
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/v1/auth/reset-password");
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
    }
}
