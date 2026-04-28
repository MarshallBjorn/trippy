package com.navrotskyi.trippyapi.dto.trip;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;

import com.navrotskyi.trippyapi.validation.DateRangeProvider;
import com.navrotskyi.trippyapi.validation.ValidDateRange;

/**
 * Request DTO dla tworzenia oraz aktualizowania węzła wycieczki (TripNode).
 * <p>
 * Walidacja sprawdzana jest na poziomie kontrolera przez {@code @Valid}.
 * Walidacja wzajemnej spójności pól (endTime &gt; startTime) jest wykonywana
 * w warstwie serwisu, bo wymaga porównania dwóch pól.
 */

@ValidDateRange
public record CreateTripNodeRequest (
    @NotNull(message = "Pole startTime jest wymagane.")
    LocalDateTime startTime,

    @NotNull(message = "Pole endTime jest wymagane.")
    LocalDateTime endTime,

    @NotBlank(message = "Nazwa węzła nie może być pusta.")
    @Size(max = 200, message = "Nazwa węzła może mieć maksymalnie 200 znaków.")
    String name,

    @Size(max = 5000, message = "Notatka może mieć maksymalnie 5000 znaków.")
    String note,

    @NotNull(message = "Pole price jest wymagane (użyj 0 jeśli węzeł nie wiąże się z kosztem).")
    @DecimalMin(value = "0.00", inclusive = true, message = "Cena nie może być ujemna.")
    @Digits(integer = 10, fraction = 2, message = "Cena może mieć maksymalnie 10 cyfr przed przecinkiem i 2 po przecinku.")
    BigDecimal price,

    boolean isSeparate
) implements DateRangeProvider {
    @Override public Temporal getRangeStart() { return startTime(); }
    @Override public Temporal getRangeEnd() { return endTime(); }
}