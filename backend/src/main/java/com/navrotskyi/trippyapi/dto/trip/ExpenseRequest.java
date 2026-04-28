package com.navrotskyi.trippyapi.dto.trip;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseRequest(
        @NotNull(message = "ID wycieczki (tripId) jest wymagane")
        UUID tripId,

        @NotBlank(message = "Tytuł wydatku nie może być pusty")
        String title,

        @NotNull(message = "Kwota wydatku musi być określona")
        @DecimalMin(value = "0.01", message = "Kwota wydatku musi być większa niż zero")
        BigDecimal price,

        @NotNull(message = "Należy określić, czy jest to wydatek osobny (isSeparate)")
        Boolean isSeparate
) {}