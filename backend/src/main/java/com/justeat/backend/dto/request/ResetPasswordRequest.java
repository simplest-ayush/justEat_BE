package com.justeat.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank(message = "Token cannot be blank")
        String token,

        @NotBlank(message = "New password cannot be blank")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
                message = "Password must be at least 8 characters and include upper, lower, digit, and special character"
        )
        String newPassword
) {
}
