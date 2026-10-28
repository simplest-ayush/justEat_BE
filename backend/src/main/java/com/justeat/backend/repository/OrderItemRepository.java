package com.justeat.backend.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.justeat.backend.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
