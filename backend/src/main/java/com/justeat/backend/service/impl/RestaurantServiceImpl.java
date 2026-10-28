package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.RestaurantRequest;
import com.justeat.backend.dto.response.RestaurantResponse;
import com.justeat.backend.dto.response.RestaurantSummaryResponse;
import com.justeat.backend.entity.Restaurant;
import com.justeat.backend.entity.User;
import com.justeat.backend.exception.DuplicateResourceException;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.exception.ForbiddenOperationException;
import com.justeat.backend.repository.RestaurantRepository;
import com.justeat.backend.repository.UserRepository;
import com.justeat.backend.service.RestaurantService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RestaurantResponse create(UUID ownerId, RestaurantRequest request) {
        log.info("Creating restaurant '{}' for ownerId={}", request.name(), ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + ownerId));

        if (restaurantRepository.existsByOwnerIdAndName(ownerId, request.name())) {
            throw new DuplicateResourceException(
                    "You already have a restaurant named '" + request.name() + "'");
        }

        Restaurant restaurant = Restaurant.builder()
                .owner(owner)
                .name(request.name())
                .cuisineType(request.cuisineType())
                .description(request.description())
                .address(request.address())
                .city(request.city())
                .phone(request.phone())
                .active(true)
                .build();

        restaurantRepository.save(restaurant);
        log.info("Restaurant created: id={}, name={}", restaurant.getId(), restaurant.getName());
        return toResponse(restaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse update(UUID ownerId, UUID restaurantId, RestaurantRequest request) {
        log.info("Updating restaurantId={} by ownerId={}", restaurantId, ownerId);

        Restaurant restaurant = getOwnedActiveRestaurant(ownerId, restaurantId);

        if (!restaurant.getName().equalsIgnoreCase(request.name())
                && restaurantRepository.existsByOwnerIdAndName(ownerId, request.name())) {
            throw new DuplicateResourceException(
                    "You already have a restaurant named '" + request.name() + "'");
        }

        restaurant.setName(request.name());
        restaurant.setCuisineType(request.cuisineType());
        restaurant.setDescription(request.description());
        restaurant.setAddress(request.address());
        restaurant.setCity(request.city());
        restaurant.setPhone(request.phone());

        restaurantRepository.save(restaurant);
        log.info("Restaurant updated: id={}", restaurant.getId());
        return toResponse(restaurant);
    }

    @Override
    @Transactional
    public void delete(UUID ownerId, UUID restaurantId) {
        log.info("Soft-deleting restaurantId={} by ownerId={}", restaurantId, ownerId);

        Restaurant restaurant = getOwnedActiveRestaurant(ownerId, restaurantId);
        restaurant.setActive(false);
        restaurant.markDeleted();
        restaurantRepository.save(restaurant);

        log.info("Restaurant deactivated: id={}", restaurant.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getById(UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> !r.isDeleted() && r.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));

        return toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getMyRestaurants(UUID ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .filter(r -> !r.isDeleted())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RestaurantSummaryResponse> browse(String query, String cuisine, String city, Pageable pageable) {
        Specification<Restaurant> spec = buildBrowseSpecification(query, cuisine, city);
        return restaurantRepository.findAll(spec, pageable).map(this::toSummary);
    }

    private Restaurant getOwnedActiveRestaurant(UUID ownerId, UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));

        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("You do not own this restaurant");
        }
        return restaurant;
    }

    private Specification<Restaurant> buildBrowseSpecification(String query, String cuisine, String city) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (query != null && !query.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + query.toLowerCase() + "%"));
            }
            if (cuisine != null && !cuisine.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("cuisineType")), cuisine.toLowerCase()));
            }
            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        User owner = restaurant.getOwner();
        String ownerDisplayName = owner.getFullName() != null ? owner.getFullName() : owner.getUsername();

        return new RestaurantResponse(
                restaurant.getId(),
                new RestaurantResponse.Owner(owner.getId(), ownerDisplayName),
                restaurant.getName(),
                restaurant.getCuisineType(),
                restaurant.getDescription(),
                restaurant.getAddress(),
                restaurant.getCity(),
                restaurant.getPhone(),
                restaurant.isActive()
        );
    }

    private RestaurantSummaryResponse toSummary(Restaurant restaurant) {
        return new RestaurantSummaryResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCuisineType(),
                restaurant.getCity()
        );
    }
}
