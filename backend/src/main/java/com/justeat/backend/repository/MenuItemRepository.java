package com.justeat.backend.repository;

import com.justeat.backend.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    boolean existsByRestaurantIdAndName(UUID restaurantId, String name);

    List<MenuItem> findByRestaurantIdAndActiveTrue(UUID restaurantId);

    List<MenuItem> findByRestaurantIdAndActiveTrueAndCategoryIgnoreCase(UUID restaurantId, String category);

    List<MenuItem> findByRestaurantIdAndActiveTrueAndMostlyOrderedTrue(UUID restaurantId);
}
