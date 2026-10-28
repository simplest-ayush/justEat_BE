package com.justeat.backend.repository;

import com.justeat.backend.entity.Order;
import com.justeat.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"orderItems", "restaurant", "customer"})
    Optional<Order> findWithDetailsById(UUID id);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}
