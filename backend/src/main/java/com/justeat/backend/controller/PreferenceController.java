package com.justeat.backend.controller;

import com.justeat.backend.dto.request.PreferenceUpdateRequest;
import com.justeat.backend.dto.response.PreferenceResponse;
import com.justeat.backend.service.PreferenceService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/preferences")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')")
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid customer id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Customer preferences not found"),
    })
    public ResponseEntity<PreferenceResponse> getPreferences(
            @AuthenticationPrincipal UUID authenticatedUserId,
            @PathVariable UUID customerId) {

        log.info("GET /api/v1/customers/{}/preferences - authenticatedUserId={}", customerId, authenticatedUserId);
        return ResponseEntity.ok(preferenceService.getPreferences(customerId));
    }

    @PutMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Customer preferences not found"),
    })
    public ResponseEntity<PreferenceResponse> updatePreferences(
            @AuthenticationPrincipal UUID authenticatedUserId,
            @PathVariable UUID customerId,
            @Valid @RequestBody PreferenceUpdateRequest request) {

        log.info("PUT /api/v1/customers/{}/preferences - authenticatedUserId={}", customerId, authenticatedUserId);
        return ResponseEntity.ok(preferenceService.updatePreferences(customerId, request));
    }

    @PostMapping("/favorites/{restaurantId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorite restaurant added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Customer or restaurant not found"),
    })
    public ResponseEntity<Void> addFavorite(
            @AuthenticationPrincipal UUID authenticatedUserId,
            @PathVariable UUID customerId,
            @PathVariable UUID restaurantId) {

        log.info("POST /api/v1/customers/{}/favorites/{} - authenticatedUserId={}", customerId, restaurantId, authenticatedUserId);
        preferenceService.addFavorite(customerId, restaurantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/favorites/{restaurantId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorite restaurant removed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Customer or restaurant not found"),
    })
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UUID authenticatedUserId,
            @PathVariable UUID customerId,
            @PathVariable UUID restaurantId) {

        log.info("DELETE /api/v1/customers/{}/favorites/{} - authenticatedUserId={}", customerId, restaurantId, authenticatedUserId);
        preferenceService.removeFavorite(customerId, restaurantId);
        return ResponseEntity.noContent().build();
    }
}
