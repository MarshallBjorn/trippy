package com.navrotskyi.trippyapi.dto;

import com.navrotskyi.trippyapi.validation.PasswordsMatch;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordsMatch
public record RegisterRequest (
    @NotBlank(message = "Name cannot be empty")
    String name,

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 128, message = "Hasło powinno zawierać od 8 do 128 znaków")
    @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character"
    )
    String password,

    @NotBlank(message = "Password confirmation cannot be empty")
    String confirmPassword
) {

}
