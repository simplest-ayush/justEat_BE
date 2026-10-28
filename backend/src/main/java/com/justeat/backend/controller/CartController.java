package com.justeat.backend.controller;

import com.justeat.backend.dto.request.AddCartItemRequest;
import com.justeat.backend.dto.request.UpdateCartItemRequest;
import com.justeat.backend.dto.response.CartResponse;
import com.justeat.backend.service.CartService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
    })
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UUID customerId) {
        log.info("GET /api/v1/cart - customerId={}", customerId);
        return ResponseEntity.ok(cartService.getCart(customerId));
    }

    @PostMapping("/items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added to cart successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Menu item not found"),
    })
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal UUID customerId,
            @Valid @RequestBody AddCartItemRequest request
    ){
        log.info("POST /api/v1/cart/items - customerId={}, menuItemId={}", customerId, request.menuItemId());
        return ResponseEntity.ok(cartService.addItem(customerId, request));
    }

    @PutMapping("/items/{itemId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Cart item not found"),
    })
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal UUID customerId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ){
        log.info("PUT /api/v1/cart/items/{} - customerId={}", itemId, customerId);
        return ResponseEntity.ok(cartService.updateItem(customerId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart item removed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Cart item not found"),
    })
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal UUID customerId,
            @PathVariable UUID itemId
    ){
        log.info("DELETE /api/v1/cart/items/{} - customerId={}", itemId, customerId);
        return ResponseEntity.ok(cartService.removeItem(customerId, itemId));
    }

    @DeleteMapping
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
    })
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UUID customerId) {
        log.info("DELETE /api/v1/cart - customerId={}", customerId);
        cartService.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
