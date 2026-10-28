package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.AddCartItemRequest;
import com.justeat.backend.dto.request.UpdateCartItemRequest;
import com.justeat.backend.dto.response.CartItemResponse;
import com.justeat.backend.dto.response.CartResponse;
import com.justeat.backend.entity.Cart;
import com.justeat.backend.entity.CartItem;
import com.justeat.backend.entity.MenuItem;
import com.justeat.backend.entity.User;
import com.justeat.backend.exception.ConflictException;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.repository.CartRepository;
import com.justeat.backend.repository.MenuItemRepository;
import com.justeat.backend.repository.UserRepository;
import com.justeat.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UUID customerId) {
        return cartRepository.findByCustomerIdWithItems(customerId)
                .map(this::toResponse)
                .orElseGet(this::emptyCart);
    }

    @Override
    @Transactional
    public CartResponse addItem(UUID customerId, AddCartItemRequest request) {
        log.info("Adding item to cart: customerId={}, menuItemId={}, quantity={}",
                customerId, request.menuItemId(), request.quantity());

        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .filter(m -> !m.isDeleted() && m.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Menu item not found: " + request.menuItemId()));

        Cart cart = cartRepository.findByCustomerIdWithItems(customerId).orElse(null);

        if (cart == null) {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));
            cart = Cart.builder()
                    .customer(customer)
                    .restaurant(menuItem.getRestaurant())
                    .build();
            cart = cartRepository.save(cart);
        } else if (!cart.getRestaurant().getId().equals(menuItem.getRestaurant().getId())) {
            throw new ConflictException(
                    "Your cart already contains items from a different restaurant. Clear the cart to switch restaurants.");
        }

        Optional<CartItem> existing = cart.getCartItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.quantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.quantity())
                    .build();
            cart.getCartItems().add(newItem);
        }

        cartRepository.save(cart);
        log.info("Item added to cart: cartId={}", cart.getId());
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(UUID customerId, UUID itemId, UpdateCartItemRequest request) {
        log.info("Updating cart item: customerId={}, itemId={}, quantity={}", customerId, itemId, request.quantity());

        Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                .orElseThrow(() -> new EntityNotFoundException("No active cart for customer: " + customerId));

        CartItem item = cart.getCartItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));

        item.setQuantity(request.quantity());
        cartRepository.save(cart);

        log.info("Cart item updated: itemId={}", itemId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(UUID customerId, UUID itemId) {
        log.info("Removing cart item: customerId={}, itemId={}", customerId, itemId);

        Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                .orElseThrow(() -> new EntityNotFoundException("No active cart for customer: " + customerId));

        CartItem item = cart.getCartItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found: " + itemId));

        cart.getCartItems().remove(item);

        if (cart.getCartItems().isEmpty()) {
            cartRepository.delete(cart);
            log.info("Cart emptied and removed: customerId={}", customerId);
            return emptyCart();
        }

        cartRepository.save(cart);
        log.info("Cart item removed: itemId={}", itemId);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart(UUID customerId) {
        log.info("Clearing cart for customerId={}", customerId);
        cartRepository.findByCustomerIdWithItems(customerId).ifPresent(cartRepository::delete);
    }

    private CartResponse emptyCart() {
        return new CartResponse(null, null, List.of(), BigDecimal.ZERO);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                new CartResponse.Restaurant(cart.getRestaurant().getId(), cart.getRestaurant().getName()),
                items,
                subtotal
        );
    }

    private CartItemResponse toItemResponse(CartItem item) {
        BigDecimal unitPrice = item.getMenuItem().getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                new CartItemResponse.MenuItem(item.getMenuItem().getId(), item.getMenuItem().getName()),
                unitPrice,
                item.getQuantity(),
                lineTotal
        );
    }
}
