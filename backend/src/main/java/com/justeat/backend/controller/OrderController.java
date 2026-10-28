package com.justeat.backend.controller;

import com.justeat.backend.dto.request.PlaceOrderRequest;
import com.justeat.backend.dto.response.OrderResponse;
import com.justeat.backend.dto.response.OrderStatusResponse;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or cart is invalid"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Required resource not found"),
    })
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UUID customerId,
            @Valid @RequestBody PlaceOrderRequest request) {

        log.info("POST /api/v1/orders - customerId={}", customerId);
        OrderResponse response = orderService.placeOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    public ResponseEntity<OrderResponse> getOrder(
            @AuthenticationPrincipal UUID customerId,
            @PathVariable UUID id) {

        log.info("GET /api/v1/orders/{} - customerId={}", id, customerId);
        return ResponseEntity.ok(orderService.getOrderForCustomer(customerId, id));
    }

    @GetMapping("/{id}/status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order id"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
    })
    public ResponseEntity<OrderStatusResponse> getOrderStatus(
            @AuthenticationPrincipal UUID customerId,
            @PathVariable UUID id) {

        log.info("GET /api/v1/orders/{}/status - customerId={}", id, customerId);
        return ResponseEntity.ok(orderService.getOrderStatus(customerId, id));
    }

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User does not have the CUSTOMER role"),
    })
    public ResponseEntity<PageResponse<OrderSummaryResponse>> getOrderHistory(
            @AuthenticationPrincipal UUID customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable) {

        log.info("GET /api/v1/orders - customerId={}, from={}, to={}, status={}", customerId, from, to, status);
        var result = orderService.getCustomerOrderHistory(customerId, from, to, status, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }
}
