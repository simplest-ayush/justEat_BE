package com.justeat.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestaurantRequest(

        @NotBlank(message = "Restaurant name cannot be blank")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        String cuisineType,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotBlank(message = "Address cannot be blank")
        @Size(max = 200, message = "Address must not exceed 200 characters")
        String address,

        String city,

        String phone
) {
}