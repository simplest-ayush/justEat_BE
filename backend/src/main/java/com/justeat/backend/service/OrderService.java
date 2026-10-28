package com.justeat.backend.service;

import com.justeat.backend.dto.request.PlaceOrderRequest;
import com.justeat.backend.dto.request.UpdateOrderStatusRequest;
import com.justeat.backend.dto.response.OrderResponse;
import com.justeat.backend.dto.response.OrderStatusResponse;
import com.justeat.backend.dto.response.OrderSummaryResponse;
import com.justeat.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface OrderService {

//    CUSTOMER FACING ORDER SERVICES

    OrderResponse placeOrder(UUID customerId, PlaceOrderRequest request);

    OrderResponse getOrderForCustomer(UUID customerId, UUID orderId);

    OrderStatusResponse getOrderStatus(UUID customerId, UUID orderId);

    Page<OrderSummaryResponse> getCustomerOrderHistory(
            UUID customerId, LocalDate from, LocalDate to, OrderStatus status, Pageable pageable);

//    OWNER FACING ORDER SERVICES

    Page<OrderSummaryResponse> getOwnerOrders(
            UUID ownerId, UUID restaurantId, OrderStatus status, Pageable pageable);

    OrderResponse getOrderForOwner(UUID ownerId, UUID orderId);

    OrderResponse updateStatus(UUID ownerId, UUID orderId, UpdateOrderStatusRequest request);
}
