package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.PreferenceUpdateRequest;
import com.justeat.backend.dto.response.FavoriteRestaurantResponse;
import com.justeat.backend.dto.response.PreferenceResponse;
import com.justeat.backend.entity.CustomerPreference;
import com.justeat.backend.entity.FavoriteRestaurant;
import com.justeat.backend.entity.Restaurant;
import com.justeat.backend.entity.User;
import com.justeat.backend.enums.DietaryRestriction;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.repository.CustomerPreferenceRepository;
import com.justeat.backend.repository.FavoriteRestaurantRepository;
import com.justeat.backend.repository.RestaurantRepository;
import com.justeat.backend.repository.UserRepository;
import com.justeat.backend.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreferenceServiceImpl implements PreferenceService {

    private final CustomerPreferenceRepository preferenceRepository;
    private final FavoriteRestaurantRepository favoriteRestaurantRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(UUID customerId) {
        Optional<CustomerPreference> preference = preferenceRepository.findByCustomerId(customerId);

        Set<String> cuisines = preference
                .map(p -> new HashSet<>(p.getPreferredCuisines()))
                .orElseGet(HashSet::new);
        Set<DietaryRestriction> restrictions = preference
                .map(p -> new HashSet<>(p.getDietaryRestrictions()))
                .orElseGet(HashSet::new);

        List<FavoriteRestaurantResponse> favorites = getFavorites(customerId);

        return new PreferenceResponse(cuisines, restrictions, favorites);
    }

    @Override
    @Transactional
    public PreferenceResponse updatePreferences(UUID customerId, PreferenceUpdateRequest request) {
        log.info("Updating preferences for customerId={}", customerId);

        CustomerPreference preference = preferenceRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));
                    return CustomerPreference.builder().customer(customer).build();
                });

        preference.setPreferredCuisines(request.preferredCuisines() != null
                ? new HashSet<>(request.preferredCuisines()) : new HashSet<>());
        preference.setDietaryRestrictions(request.dietaryRestrictions() != null
                ? new HashSet<>(request.dietaryRestrictions()) : new HashSet<>());

        preferenceRepository.save(preference);
        log.info("Preferences updated for customerId={}", customerId);

        List<FavoriteRestaurantResponse> favorites = getFavorites(customerId);
        return new PreferenceResponse(
                new HashSet<>(preference.getPreferredCuisines()),
                new HashSet<>(preference.getDietaryRestrictions()),
                favorites);
    }

    @Override
    @Transactional
    public void addFavorite(UUID customerId, UUID restaurantId) {
        log.info("Adding favorite: customerId={}, restaurantId={}", customerId, restaurantId);

        if (favoriteRestaurantRepository.existsByCustomerIdAndRestaurantId(customerId, restaurantId)) {
            log.info("Restaurant already favorited: customerId={}, restaurantId={}", customerId, restaurantId);
            return;
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + customerId));
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));

        FavoriteRestaurant favorite = FavoriteRestaurant.builder()
                .customer(customer)
                .restaurant(restaurant)
                .build();
        favoriteRestaurantRepository.save(favorite);

        log.info("Favorite added: customerId={}, restaurantId={}", customerId, restaurantId);
    }

    @Override
    @Transactional
    public void removeFavorite(UUID customerId, UUID restaurantId) {
        log.info("Removing favorite: customerId={}, restaurantId={}", customerId, restaurantId);
        favoriteRestaurantRepository.deleteByCustomerIdAndRestaurantId(customerId, restaurantId);
    }

    private List<FavoriteRestaurantResponse> getFavorites(UUID customerId) {
        return favoriteRestaurantRepository.findByCustomerId(customerId).stream()
                .map(fav -> new FavoriteRestaurantResponse(
                        new FavoriteRestaurantResponse.Restaurant(fav.getRestaurant().getId(), fav.getRestaurant().getName()),
                        fav.getCreatedAt()))
                .toList();
    }
}
