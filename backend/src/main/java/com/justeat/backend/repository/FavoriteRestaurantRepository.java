package com.justeat.backend.repository;

import com.justeat.backend.entity.FavoriteRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FavoriteRestaurantRepository extends JpaRepository<FavoriteRestaurant, UUID> {

    List<FavoriteRestaurant> findByCustomerId(UUID customerId);

    boolean existsByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId);

    void deleteByCustomerIdAndRestaurantId(UUID customerId, UUID restaurantId);
}
