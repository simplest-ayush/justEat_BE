package com.justeat.backend.controller;

import com.justeat.backend.dto.response.MenuItemResponse;
import com.justeat.backend.service.MenuItemService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants/{restaurantId}/menu-items")
@RequiredArgsConstructor
@Slf4j
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Menu items retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
    })
    public ResponseEntity<List<MenuItemResponse>> getMenu(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) String category
    ){
        log.info("REST request to get all menu items for restaurant {} - category={}", restaurantId, category);
        return ResponseEntity.ok(menuItemService.getByRestaurant(restaurantId, category));
    }

    @GetMapping("/mostly-ordered")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mostly ordered menu items retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
    })
    public ResponseEntity<List<MenuItemResponse>> getMostlyOrdered(
            @PathVariable UUID restaurantId
    ){
        log.info("GET /api/v1/restaurants/{}/menu-items/mostly-ordered", restaurantId);
        return ResponseEntity.ok(menuItemService.getMostlyOrdered(restaurantId));
    }
}
