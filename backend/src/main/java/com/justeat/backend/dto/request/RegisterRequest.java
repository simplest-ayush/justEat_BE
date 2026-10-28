package com.justeat.backend.dto.request;

import com.justeat.backend.enums.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank(message = "Username cannot be blank")
        @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
        String username,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "Password must be at least 8 characters and include upper, lower, digit, and special character"
        )
        String password,

        String fullName,

        @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
        String phone,

        @NotNull(message = "Role is required")
        Role role
) {
}