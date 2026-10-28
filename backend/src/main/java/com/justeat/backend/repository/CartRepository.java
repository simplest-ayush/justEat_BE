package com.justeat.backend.repository;

import com.justeat.backend.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.customer.id = :customerId")
    Optional<Cart> findByCustomerIdWithItems(UUID customerId);
}
