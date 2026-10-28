package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.PlaceOrderRequest;
import com.justeat.backend.dto.request.UpdateOrderStatusRequest;
import com.justeat.backend.dto.response.OrderResponse;
import com.justeat.backend.entity.*;
import com.justeat.backend.enums.OrderStatus;
import com.justeat.backend.exception.ConflictException;
import com.justeat.backend.exception.ForbiddenOperationException;
import com.justeat.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusHistoryRepository orderStatusHistoryRepository;
    @Mock private CartRepository cartRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;
    @Mock private MenuItemRepository menuItemRepository;

    @InjectMocks private OrderServiceImpl orderService;

    private User customer;
    private User owner;
    private Restaurant restaurant;
    private UUID customerId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        customer = User.builder().id(customerId).build();
        owner = User.builder().id(ownerId).build();
        restaurant = Restaurant.builder().id(UUID.randomUUID()).owner(owner).name("Test Restaurant").build();
    }

    @Test
    void placeOrder_withEmptyCart_throwsConflictException() {
        Cart emptyCart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurant)
                .cartItems(new ArrayList<>()).build();

        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() -> orderService.placeOrder(customerId, new PlaceOrderRequest("123 Main St")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("empty cart");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_withNoCartAtAll_throwsConflictException() {
        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(customerId, new PlaceOrderRequest("123 Main St")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void placeOrder_withInactiveMenuItemInCart_throwsConflictException() {
        MenuItem inactiveItem = MenuItem.builder()
                .id(UUID.randomUUID()).name("Discontinued Dish").active(false).build();
        CartItem cartItem = CartItem.builder().menuItem(inactiveItem).quantity(1).build();
        Cart cart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurant)
                .cartItems(List.of(cartItem)).build();

        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.placeOrder(customerId, new PlaceOrderRequest("123 Main St")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("no longer available");
    }

    @Test
    void getOrderForCustomer_whenOrderBelongsToDifferentCustomer_throwsForbiddenOperationException() {
        UUID otherCustomerId = UUID.randomUUID();
        User otherCustomer = User.builder().id(otherCustomerId).build();
        Order order = Order.builder()
                .id(UUID.randomUUID()).customer(otherCustomer).restaurant(restaurant)
                .status(OrderStatus.PENDING).orderItems(new ArrayList<>()).build();

        when(orderRepository.findWithDetailsById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderForCustomer(customerId, order.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getOrderForOwner_whenOrderNotForOwnersRestaurant_throwsForbiddenOperationException() {
        UUID differentOwnerId = UUID.randomUUID();
        Order order = Order.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurant)
                .status(OrderStatus.PENDING).orderItems(new ArrayList<>()).build();

        when(orderRepository.findWithDetailsById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderForOwner(differentOwnerId, order.getId()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void updateStatus_withLegalTransition_updatesOrderAndRecordsHistory() {
        Order order = Order.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurant)
                .status(OrderStatus.PENDING).orderItems(new ArrayList<>()).build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(order.getId()))
                .thenReturn(List.of());

        OrderResponse response = orderService.updateStatus(
                ownerId, order.getId(), new UpdateOrderStatusRequest(OrderStatus.PREPARING));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
        verify(orderStatusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void updateStatus_withIllegalTransition_throwsConflictExceptionAndDoesNotSave() {
        Order order = Order.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurant)
                .status(OrderStatus.PENDING).orderItems(new ArrayList<>()).build();

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(
                ownerId, order.getId(), new UpdateOrderStatusRequest(OrderStatus.READY)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Illegal");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING); // unchanged
        verify(orderRepository, never()).save(any());
        verify(orderStatusHistoryRepository, never()).save(any());
    }
}