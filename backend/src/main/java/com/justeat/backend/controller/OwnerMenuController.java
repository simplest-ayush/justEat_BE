package com.justeat.backend.controller;

import com.justeat.backend.dto.request.MarkSpecialRequest;
import com.justeat.backend.dto.request.MenuItemRequest;
import com.justeat.backend.dto.response.MenuItemResponse;
import com.justeat.backend.service.MenuItemService;
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
@RequestMapping("/api/v1/owner/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class OwnerMenuController {

    private final MenuItemService menuItemService;

    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Menu item created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
    })
    public ResponseEntity<MenuItemResponse> create(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuItemRequest request
            ){
        log.info("POST /api/v1/owner/restaurants/{}/menu-items - ownerId={}", restaurantId, ownerId);
        MenuItemResponse response=menuItemService.create(ownerId, restaurantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{itemId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Restaurant or menu item not found"),
    })
    public ResponseEntity<MenuItemResponse> update(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuItemRequest request
    ){
        log.info("PUT /api/v1/owner/restaurants/{}/menu-items/{} - ownerId={}", restaurantId, itemId, ownerId);
        return ResponseEntity.ok(menuItemService.update(ownerId, restaurantId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Menu item deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Restaurant or menu item not found"),
    })
    public ResponseEntity<MenuItemResponse> delete(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId
    ){
        log.info("DELETE /api/v1/owner/restaurants/{}/menu-items/{} - ownerId={}", restaurantId, itemId, ownerId);
        menuItemService.delete(ownerId, restaurantId, itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{itemId}/special")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu item special status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Restaurant or menu item not found"),
    })
    public ResponseEntity<MenuItemResponse> markSpecial(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MarkSpecialRequest request
    ){
        log.info("PATCH /api/v1/owner/restaurants/{}/menu-items/{}/special - ownerId={}", restaurantId, itemId, ownerId);
        return ResponseEntity.ok(menuItemService.markSpecial(ownerId, restaurantId, itemId, request));
    }
}
