package com.navrotskyi.trippyapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest (
    @NotBlank(message = "[ERROR] Name cannot be empty.")
    @Size(min = 2, max = 50, message = "[ERROR] Name must be between 2 and 50 characters")
    String name,

    @NotBlank(message = "[ERROR] Email cannot be empty.")
    @Email(message = "[ERROR] Email must be correct.")
    String email,

    @NotBlank(message = "[ERROR] Password is required")
    @Size(min = 8, max = 128, message = "[ERROR] Password must be between 8 and 128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "[ERROR] Password must contain at least one uppercase, one lowercase, one digit, and one special character"
    )
    String password
) { 
}
