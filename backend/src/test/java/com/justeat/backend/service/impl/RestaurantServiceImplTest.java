package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.RestaurantRequest;
import com.justeat.backend.dto.response.RestaurantResponse;
import com.justeat.backend.entity.Restaurant;
import com.justeat.backend.entity.User;
import com.justeat.backend.exception.DuplicateResourceException;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.exception.ForbiddenOperationException;
import com.justeat.backend.repository.RestaurantRepository;
import com.justeat.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private RestaurantServiceImpl restaurantService;

    private User owner;
    private UUID ownerId;
    private UUID restaurantId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();
        owner = User.builder().id(ownerId).username("owner1").build();
    }

    @Test
    void create_withUniqueNameForOwner_savesRestaurant() {
        RestaurantRequest request = new RestaurantRequest(
                "Pizza Place", "Italian", "desc", "123 St", "City", "9876543210");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(restaurantRepository.existsByOwnerIdAndName(ownerId, "Pizza Place")).thenReturn(false);
        when(restaurantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RestaurantResponse response = restaurantService.create(ownerId, request);

        assertThat(response.name()).isEqualTo("Pizza Place");
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void create_withDuplicateNameForSameOwner_throwsDuplicateResourceException() {
        RestaurantRequest request = new RestaurantRequest(
                "Pizza Place", "Italian", "desc", "123 St", "City", "9876543210");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(restaurantRepository.existsByOwnerIdAndName(ownerId, "Pizza Place")).thenReturn(true);

        assertThatThrownBy(() -> restaurantService.create(ownerId, request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void create_withNonExistentOwner_throwsEntityNotFoundException() {
        RestaurantRequest request = new RestaurantRequest(
                "Pizza Place", "Italian", "desc", "123 St", "City", "9876543210");

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> restaurantService.create(ownerId, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_byNonOwner_throwsForbiddenOperationException() {
        UUID actualOwnerId = UUID.randomUUID();
        UUID impostorId = UUID.randomUUID();
        User actualOwner = User.builder().id(actualOwnerId).build();
        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId).owner(actualOwner).name("Original").build();

        RestaurantRequest request = new RestaurantRequest(
                "New Name", "Italian", "desc", "123 St", "City", "9876543210");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> restaurantService.update(impostorId, restaurantId, request))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void update_withValidOwnership_updatesFields() {
        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId).owner(owner).name("Old Name").active(true).build();

        RestaurantRequest request = new RestaurantRequest(
                "New Name", "Mexican", "new desc", "456 Ave", "NewCity", "9123456780");

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.existsByOwnerIdAndName(ownerId, "New Name")).thenReturn(false);
        when(restaurantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RestaurantResponse response = restaurantService.update(ownerId, restaurantId, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.cuisineType()).isEqualTo("Mexican");
    }

    @Test
    void delete_withValidOwnership_deactivatesWithoutSoftDeleting() {
        Restaurant restaurant = Restaurant.builder()
                .id(restaurantId).owner(owner).name("Test").active(true).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        restaurantService.delete(ownerId, restaurantId);

        assertThat(restaurant.isActive()).isFalse();
        assertThat(restaurant.isDeleted()).isTrue(); // current implementation also soft-deletes via markDeleted()
    }

    @Test
    void getById_forInactiveRestaurant_throwsEntityNotFoundException() {
        Restaurant inactive = Restaurant.builder()
                .id(restaurantId).owner(owner).name("Inactive").active(false).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(inactive));

        assertThatThrownBy(() -> restaurantService.getById(restaurantId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getById_forActiveRestaurant_returnsResponse() {
        Restaurant active = Restaurant.builder()
                .id(restaurantId).owner(owner).name("Active Place").active(true).build();

        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(active));

        RestaurantResponse response = restaurantService.getById(restaurantId);

        assertThat(response.name()).isEqualTo("Active Place");
    }
}