package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.AddCartItemRequest;
import com.justeat.backend.dto.request.UpdateCartItemRequest;
import com.justeat.backend.dto.response.CartResponse;
import com.justeat.backend.entity.*;
import com.justeat.backend.exception.ConflictException;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.repository.CartRepository;
import com.justeat.backend.repository.MenuItemRepository;
import com.justeat.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private MenuItemRepository menuItemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private CartServiceImpl cartService;

    private UUID customerId;
    private User customer;
    private Restaurant restaurantA;
    private Restaurant restaurantB;
    private MenuItem itemFromA;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        customer = User.builder().id(customerId).build();
        restaurantA = Restaurant.builder().id(UUID.randomUUID()).name("Restaurant A").build();
        restaurantB = Restaurant.builder().id(UUID.randomUUID()).name("Restaurant B").build();
        itemFromA = MenuItem.builder()
                .id(UUID.randomUUID()).restaurant(restaurantA).name("Pizza")
                .price(BigDecimal.valueOf(10)).active(true).build();
    }

    @Test
    void addItem_toEmptyCart_createsNewCart() {
        AddCartItemRequest request = new AddCartItemRequest(itemFromA.getId(), 2);

        when(menuItemRepository.findById(itemFromA.getId())).thenReturn(Optional.of(itemFromA));
        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.empty());
        when(userRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.addItem(customerId, request);

        assertThat(response.restaurant().name()).isEqualTo("Restaurant A");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void addItem_fromDifferentRestaurantThanExistingCart_throwsConflictException() {
        MenuItem itemFromB = MenuItem.builder()
                .id(UUID.randomUUID()).restaurant(restaurantB).name("Tacos")
                .price(BigDecimal.valueOf(8)).active(true).build();

        Cart existingCart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurantA)
                .cartItems(new ArrayList<>()).build();

        AddCartItemRequest request = new AddCartItemRequest(itemFromB.getId(), 1);

        when(menuItemRepository.findById(itemFromB.getId())).thenReturn(Optional.of(itemFromB));
        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(existingCart));

        assertThatThrownBy(() -> cartService.addItem(customerId, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("different restaurant");
    }

    @Test
    void addItem_sameItemTwice_incrementsQuantityInsteadOfDuplicating() {
        CartItem existingLine = CartItem.builder()
                .id(UUID.randomUUID()).menuItem(itemFromA).quantity(1).build();
        Cart existingCart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurantA)
                .cartItems(new ArrayList<>(List.of(existingLine))).build();
        existingLine.setCart(existingCart);

        AddCartItemRequest request = new AddCartItemRequest(itemFromA.getId(), 3);

        when(menuItemRepository.findById(itemFromA.getId())).thenReturn(Optional.of(itemFromA));
        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.addItem(customerId, request);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(4); // 1 + 3, not two rows
    }

    @Test
    void addItem_forInactiveMenuItem_throwsEntityNotFoundException() {
        MenuItem inactiveItem = MenuItem.builder()
                .id(UUID.randomUUID()).restaurant(restaurantA).active(false).build();

        AddCartItemRequest request = new AddCartItemRequest(inactiveItem.getId(), 1);

        when(menuItemRepository.findById(inactiveItem.getId())).thenReturn(Optional.of(inactiveItem));

        assertThatThrownBy(() -> cartService.addItem(customerId, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateItem_withValidQuantity_updatesLineTotal() {
        CartItem line = CartItem.builder().id(UUID.randomUUID()).menuItem(itemFromA).quantity(1).build();
        Cart cart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurantA)
                .cartItems(new ArrayList<>(List.of(line))).build();

        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.updateItem(customerId, line.getId(), request);

        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        assertThat(response.items().get(0).lineTotal()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void updateItem_forNonExistentItemId_throwsEntityNotFoundException() {
        Cart cart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurantA)
                .cartItems(new ArrayList<>()).build();

        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> cartService.updateItem(customerId, UUID.randomUUID(), new UpdateCartItemRequest(1)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeItem_lastItemInCart_deletesEntireCart() {
        CartItem onlyItem = CartItem.builder().id(UUID.randomUUID()).menuItem(itemFromA).quantity(1).build();
        Cart cart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurantA)
                .cartItems(new ArrayList<>(List.of(onlyItem))).build();

        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.removeItem(customerId, onlyItem.getId());

        assertThat(response.items()).isEmpty();
        verify(cartRepository).delete(cart);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItem_oneOfMultipleItems_keepsCartWithRemainingItems() {
        CartItem item1 = CartItem.builder().id(UUID.randomUUID()).menuItem(itemFromA).quantity(1).build();
        MenuItem itemB = MenuItem.builder()
                .id(UUID.randomUUID()).restaurant(restaurantA).price(BigDecimal.valueOf(5)).build();
        CartItem item2 = CartItem.builder().id(UUID.randomUUID()).menuItem(itemB).quantity(2).build();

        Cart cart = Cart.builder()
                .id(UUID.randomUUID()).customer(customer).restaurant(restaurantA)
                .cartItems(new ArrayList<>(List.of(item1, item2))).build();

        when(cartRepository.findByCustomerIdWithItems(customerId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CartResponse response = cartService.removeItem(customerId, item1.getId());

        assertThat(response.items()).hasSize(1);
        verify(cartRepository, never()).delete(any());
    }
}