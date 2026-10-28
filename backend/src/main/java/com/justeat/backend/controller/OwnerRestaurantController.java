package com.justeat.backend.controller;

import com.justeat.backend.dto.request.RestaurantRequest;
import com.justeat.backend.dto.response.RestaurantResponse;
import com.justeat.backend.service.RestaurantService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/restaurants")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class OwnerRestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurant created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
    })
    public ResponseEntity<RestaurantResponse> create(
            @AuthenticationPrincipal UUID ownerId,
            @Valid @RequestBody RestaurantRequest request
            ){
        log.info("POST /api/v1/owner/restaurants - ownerId={}", ownerId);
        RestaurantResponse response = restaurantService.create(ownerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurant updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
    })
    public ResponseEntity<RestaurantResponse> update(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id,
            @Valid @RequestBody RestaurantRequest request) {

        log.info("PUT /api/v1/owner/restaurants/{} - ownerId={}", id, ownerId);
        return ResponseEntity.ok(restaurantService.update(ownerId, id, request));
    }

    @DeleteMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid restaurant id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id) {

        log.info("DELETE /api/v1/owner/restaurants/{} - ownerId={}", id, ownerId);
        restaurantService.delete(ownerId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Owner restaurants retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
    })
    public ResponseEntity<List<RestaurantResponse>> getMyRestaurants(@AuthenticationPrincipal UUID ownerId) {
        log.info("GET /api/v1/owner/restaurants - ownerId={}", ownerId);
        return ResponseEntity.ok(restaurantService.getMyRestaurants(ownerId));
    }
}
