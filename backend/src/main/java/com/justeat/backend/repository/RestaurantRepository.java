package com.justeat.backend.repository;

import com.justeat.backend.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID>, JpaSpecificationExecutor<Restaurant> {

    boolean existsByOwnerIdAndName(UUID ownerId, String name);

    List<Restaurant> findByOwnerId(UUID ownerId);
}
