package com.justeat.backend.service.impl;

import com.justeat.backend.dto.request.MarkSpecialRequest;
import com.justeat.backend.dto.request.MenuItemRequest;
import com.justeat.backend.dto.response.MenuItemResponse;
import com.justeat.backend.entity.MenuItem;
import com.justeat.backend.entity.Restaurant;
import com.justeat.backend.exception.DuplicateResourceException;
import com.justeat.backend.exception.EntityNotFoundException;
import com.justeat.backend.exception.ForbiddenOperationException;
import com.justeat.backend.repository.MenuItemRepository;
import com.justeat.backend.repository.RestaurantRepository;
import com.justeat.backend.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public MenuItemResponse create(UUID ownerId, UUID restaurantId, MenuItemRequest request) {
        log.info("Creating menu item '{}' for restaurantId={} by ownerId={}", request.name(), restaurantId, ownerId);

        Restaurant restaurant = getOwnedRestaurant(ownerId, restaurantId);

        if (menuItemRepository.existsByRestaurantIdAndName(restaurantId, request.name())) {
            throw new DuplicateResourceException(
                    "Menu item '" + request.name() + "' already exists for this restaurant");
        }

        MenuItem menuItem = MenuItem.builder()
                .restaurant(restaurant)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(request.category())
                .active(true)
                .special(false)
                .mostlyOrdered(false)
                .build();

        menuItemRepository.save(menuItem);
        log.info("Menu item created: id={}, name={}", menuItem.getId(), menuItem.getName());
        return toResponse(menuItem);
    }

    @Override
    @Transactional
    public MenuItemResponse update(UUID ownerId, UUID restaurantId, UUID itemId, MenuItemRequest request) {
        log.info("Updating menu item id={} for restaurantId={} by ownerId={}", itemId, restaurantId, ownerId);

        getOwnedRestaurant(ownerId, restaurantId);
        MenuItem menuItem = getMenuItemInRestaurant(restaurantId, itemId);

        if (!menuItem.getName().equalsIgnoreCase(request.name())
                && menuItemRepository.existsByRestaurantIdAndName(restaurantId, request.name())) {
            throw new DuplicateResourceException(
                    "Menu item '" + request.name() + "' already exists for this restaurant");
        }

        menuItem.setName(request.name());
        menuItem.setDescription(request.description());
        menuItem.setPrice(request.price());
        menuItem.setCategory(request.category());

        menuItemRepository.save(menuItem);
        log.info("Menu item updated: id={}", menuItem.getId());
        return toResponse(menuItem);
    }

    @Override
    @Transactional
    public void delete(UUID ownerId, UUID restaurantId, UUID itemId) {
        log.info("Soft-deleting menu item id={} for restaurantId={} by ownerId={}", itemId, restaurantId, ownerId);

        getOwnedRestaurant(ownerId, restaurantId);
        MenuItem menuItem = getMenuItemInRestaurant(restaurantId, itemId);

        menuItem.setActive(false);
        menuItem.markDeleted();
        menuItemRepository.save(menuItem);

        log.info("Menu item deactivated: id={}", menuItem.getId());
    }

    @Override
    @Transactional
    public MenuItemResponse markSpecial(UUID ownerId, UUID restaurantId, UUID itemId, MarkSpecialRequest request) {
        log.info("Marking special for menu item id={} restaurantId={} by ownerId={}", itemId, restaurantId, ownerId);

        getOwnedRestaurant(ownerId, restaurantId);
        MenuItem menuItem = getMenuItemInRestaurant(restaurantId, itemId);

        menuItem.setSpecial(request.special());
        menuItem.setSpecialUntil(request.special() ? request.specialUntil() : null);

        menuItemRepository.save(menuItem);
        log.info("Menu item special flag updated: id={}, special={}", menuItem.getId(), menuItem.isSpecial());
        return toResponse(menuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getByRestaurant(UUID restaurantId, String category) {
        ensureRestaurantVisible(restaurantId);

        List<MenuItem> items = (category != null && !category.isBlank())
                ? menuItemRepository.findByRestaurantIdAndActiveTrueAndCategoryIgnoreCase(restaurantId, category)
                : menuItemRepository.findByRestaurantIdAndActiveTrue(restaurantId);

        return items.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMostlyOrdered(UUID restaurantId) {
        ensureRestaurantVisible(restaurantId);

        return menuItemRepository.findByRestaurantIdAndActiveTrueAndMostlyOrderedTrue(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Restaurant getOwnedRestaurant(UUID ownerId, UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));

        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenOperationException("You do not own this restaurant");
        }
        return restaurant;
    }

    private void ensureRestaurantVisible(UUID restaurantId) {
        restaurantRepository.findById(restaurantId)
                .filter(r -> !r.isDeleted() && r.isActive())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found: " + restaurantId));
    }

    private MenuItem getMenuItemInRestaurant(UUID restaurantId, UUID itemId) {
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .filter(m -> !m.isDeleted() && m.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new EntityNotFoundException("Menu item not found: " + itemId));
        return menuItem;
    }

    private MenuItemResponse toResponse(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getRestaurant().getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.isActive(),
                menuItem.isSpecial(),
                menuItem.getSpecialUntil(),
                menuItem.isMostlyOrdered()
        );
    }
}
