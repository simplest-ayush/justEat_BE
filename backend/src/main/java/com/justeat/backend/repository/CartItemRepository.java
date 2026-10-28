package com.justeat.backend.repository;

import com.justeat.backend.entity.CartItem;
import com.justeat.backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartIdAndMenuItem(UUID cartId, MenuItem menuItem);

}
