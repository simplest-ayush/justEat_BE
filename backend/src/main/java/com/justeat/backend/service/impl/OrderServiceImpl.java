package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.PlaceOrderRequest;
import com.justeat.backend.dto.request.UpdateOrderStatusRequest;
import com.justeat.backend.dto.response.OrderItemResponse;
import com.justeat.backend.dto.response.OrderResponse;
import com.justeat.backend.dto.response.OrderStatusHistoryResponse;
import com.justeat.backend.dto.response.OrderStatusResponse;
import com.justeat.backend.dto.response.OrderSummaryResponse;
import com.justeat.backend.entity.*;
import com.justeat.backend.enums.OrderStatus;
import com.justeat.backend.exception.ConflictException;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.exception.ForbiddenOperationException;
import com.justeat.backend.repository.*;
import com.justeat.backend.service.OrderService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CartRepository cartRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int MOSTLY_ORDERED_THRESHOLD = 3;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.PENDING, Set.of(OrderStatus.PREPARING),
        OrderStatus.PREPARING, Set.of(OrderStatus.READY),
        OrderStatus.READY, Set.of()
    );

    @Override
    @Transactional
    public OrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {
        log.info("Placing order for customerId={}", customerId);

        Cart cart = cartRepository.findByCustomerIdWithItems(customerId)
                .orElseThrow(() -> new ConflictException("Cannot place an order with an empty cart"));

        if (cart.getCartItems().isEmpty()) {
            throw new ConflictException("Cannot place an order with an empty cart");
        }

        for (CartItem cartItem : cart.getCartItems()) {
            if (!cartItem.getMenuItem().isActive() || cartItem.getMenuItem().isDeleted()) {
                throw new ConflictException(
                        "Menu item '" + cartItem.getMenuItem().getName() + "' is no longer available");
            }
        }
        User customer = userRepository
                .findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));

        BigDecimal totalAmount = cart.getCartItems().stream()
                .map(ci -> ci.getMenuItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .orderNumber(generateUniqueOrderNumber())
                .customer(customer)
                .restaurant(cart.getRestaurant())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryAddress(request.deliveryAddress())
                .orderItems(new ArrayList<>())
                .build();

        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(ci -> OrderItem.builder()
                        .order(order)
                        .menuItem(ci.getMenuItem())
                        .itemNameSnapshot(ci.getMenuItem().getName())
                        .priceSnapshot(ci.getMenuItem().getPrice())
                        .quantity(ci.getQuantity())
                        .lineTotal(ci.getMenuItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                        .build())
                .toList();
        order.getOrderItems().addAll(orderItems);

        orderRepository.save(order);

        OrderStatusHistory initialHistory = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(null)
                .toStatus(OrderStatus.PENDING)
                .changedBy(customer)
                .build();
        orderStatusHistoryRepository.save(initialHistory);

        // Update the "Mostly Ordered" counter for each distinct item in this order -
        // increments by 1 per item regardless of quantity ordered, per design.
        for (CartItem cartItem : cart.getCartItems()) {
            MenuItem menuItem = cartItem.getMenuItem();
            menuItem.setOrderCount(menuItem.getOrderCount() + 1);
            menuItem.setMostlyOrdered(menuItem.getOrderCount() > MOSTLY_ORDERED_THRESHOLD);
            menuItemRepository.save(menuItem);
        }

        cartRepository.delete(cart);

        log.info("Order placed: orderId={}, orderNumber={}, total={}", order.getId(), order.getOrderNumber(), totalAmount);
        return toResponse(order, List.of(initialHistory));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForCustomer(UUID customerId, UUID orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenOperationException("This order does not belong to you");
        }

        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);
        return toResponse(order, history);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(UUID customerId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenOperationException("This order does not belong to you");
        }

        return new OrderStatusResponse(order.getId(), order.getStatus(), order.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getCustomerOrderHistory(
            UUID customerId, LocalDate from, LocalDate to, OrderStatus status, Pageable pageable) {

        Specification<Order> spec = buildOrderSpecification(customerId, null, status, from, to);
        return orderRepository.findAll(spec, pageable).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getOwnerOrders(UUID ownerId, UUID restaurantId, OrderStatus status, Pageable pageable) {
        List<UUID> restaurantIds;

        if (restaurantId != null) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .filter(r -> !r.isDeleted())
                    .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));
            if (!restaurant.getOwner().getId().equals(ownerId)) {
                throw new ForbiddenOperationException("You do not own this restaurant");
            }
            restaurantIds = List.of(restaurantId);
        } else {
            restaurantIds = restaurantRepository.findByOwnerId(ownerId).stream()
                    .map(Restaurant::getId)
                    .toList();
            if (restaurantIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        Specification<Order> spec = buildOrderSpecification(null, restaurantIds, status, null, null);
        return orderRepository.findAll(spec, pageable).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForOwner(UUID ownerId, UUID orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("This order is not for one of your restaurants");
        }

        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);
        return toResponse(order, history);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID ownerId, UUID orderId, UpdateOrderStatusRequest request) {
        log.info("Updating order status: orderId={}, ownerId={}, newStatus={}", orderId, ownerId, request.newStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getRestaurant().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("This order is not for one of your restaurants");
        }

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.newStatus();

        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            throw new ConflictException(
                    "Illegal order status transition: " + currentStatus + " -> " + newStatus);
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + ownerId));

        order.setStatus(newStatus);
        orderRepository.save(order);

        OrderStatusHistory historyEntry = OrderStatusHistory.builder()
                .order(order)
                .fromStatus(currentStatus)
                .toStatus(newStatus)
                .changedBy(owner)
                .build();
        orderStatusHistoryRepository.save(historyEntry);

        log.info("Order status updated: orderId={}, {} -> {}", orderId, currentStatus, newStatus);

        List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderIdOrderByChangedAtAsc(orderId);
        return toResponse(order, history);
    }

    private String generateUniqueOrderNumber() {
        String candidate;
        do {
            candidate = "ORD" + (100000 + RANDOM.nextInt(900000));
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }

    private Specification<Order> buildOrderSpecification(
            UUID customerId, List<UUID> restaurantIds, OrderStatus status, LocalDate from, LocalDate to) {

        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (restaurantIds != null) {
                predicates.add(root.get("restaurant").get("id").in(restaurantIds));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), from.atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(
                        root.get("createdAt"), to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private OrderResponse toResponse(Order order, List<OrderStatusHistory> history) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(
                        oi.getId(),
                        new OrderItemResponse.MenuItem(
                                oi.getMenuItem() != null ? oi.getMenuItem().getId() : null,
                                oi.getItemNameSnapshot()
                        ),
                        oi.getPriceSnapshot(),
                        oi.getQuantity(),
                        oi.getLineTotal()))
                .toList();

        List<OrderStatusHistoryResponse> historyResponses = history.stream()
                .map(h -> new OrderStatusHistoryResponse(
                        h.getFromStatus(),
                        h.getToStatus(),
                        h.getChangedBy().getUsername(),
                        h.getChangedAt()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                new OrderResponse.Restaurant(order.getRestaurant().getId(), order.getRestaurant().getName()),
                order.getStatus(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                items,
                historyResponses,
                order.getCreatedAt()
        );
    }

    private OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                new OrderSummaryResponse.Restaurant(order.getRestaurant().getId(), order.getRestaurant().getName()),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}
