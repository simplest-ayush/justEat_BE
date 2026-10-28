package com.justeat.backend.controller;

import com.justeat.backend.dto.request.UpdateOrderStatusRequest;
import com.justeat.backend.dto.response.OrderResponse;
import com.justeat.backend.dto.response.OrderSummaryResponse;
import com.justeat.backend.dto.response.PageResponse;
import com.justeat.backend.enums.OrderStatus;
import com.justeat.backend.service.OrderService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
public class OwnerOrderController {

    private final OrderService orderService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Owner order list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
    })
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOwnerOrders(
            @AuthenticationPrincipal UUID ownerId,
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        log.info("GET /api/v1/owner/orders - ownerId={}, restaurantId={}, status={}", ownerId, restaurantId, status);
        var result = orderService.getOwnerOrders(ownerId, restaurantId, status, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id) {

        log.info("GET /api/v1/owner/orders/{} - ownerId={}", id, ownerId);
        return ResponseEntity.ok(orderService.getOrderForOwner(ownerId, id));
    }

    @PatchMapping("/{id}/status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the RESTAURANT_OWNER role"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        log.info("PATCH /api/v1/owner/orders/{}/status - ownerId={}, newStatus={}", id, ownerId, request.newStatus());
        return ResponseEntity.ok(orderService.updateStatus(ownerId, id, request));
    }
}
