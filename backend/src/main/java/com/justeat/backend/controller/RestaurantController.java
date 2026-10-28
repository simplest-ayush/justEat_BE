package com.justeat.backend.controller;

import com.justeat.backend.dto.response.PageResponse;
import com.justeat.backend.dto.response.RestaurantResponse;
import com.justeat.backend.dto.response.RestaurantSummaryResponse;
import com.justeat.backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Slf4j
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search or pagination parameters"),
    })
    public ResponseEntity<PageResponse<RestaurantSummaryResponse>> browse(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String city,
            Pageable pageable
    ){
        log.info("GET /api/v1/restaurants - query={}, cuisine={}, city={}", query, cuisine, city);
        var result=restaurantService.browse(query, cuisine, city, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurant retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid restaurant id"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
    })
    public ResponseEntity<RestaurantResponse> getById(@PathVariable UUID id) {
        log.info("GET /api/v1/restaurants/{}", id);
        return ResponseEntity.ok(restaurantService.getById(id));
    }

}
