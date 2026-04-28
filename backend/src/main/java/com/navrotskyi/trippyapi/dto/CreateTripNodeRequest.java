package com.navrotskyi.trippyapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO dla tworzenia oraz aktualizowania węzła wycieczki (TripNode).
 * <p>
 * Walidacja sprawdzana jest na poziomie kontrolera przez {@code @Valid}.
 * Walidacja wzajemnej spójności pól (endTime &gt; startTime) jest wykonywana
 * w warstwie serwisu, bo wymaga porównania dwóch pól.
 */
public class CreateTripNodeRequest {

    @NotNull(message = "Pole startTime jest wymagane.")
    private LocalDateTime startTime;

    @NotNull(message = "Pole endTime jest wymagane.")
    private LocalDateTime endTime;

    @NotBlank(message = "Nazwa węzła nie może być pusta.")
    @Size(max = 200, message = "Nazwa węzła może mieć maksymalnie 200 znaków.")
    private String name;

    @Size(max = 5000, message = "Notatka może mieć maksymalnie 5000 znaków.")
    private String note;

    @NotNull(message = "Pole price jest wymagane (użyj 0 jeśli węzeł nie wiąże się z kosztem).")
    @DecimalMin(value = "0.00", inclusive = true, message = "Cena nie może być ujemna.")
    @Digits(integer = 10, fraction = 2, message = "Cena może mieć maksymalnie 10 cyfr przed przecinkiem i 2 po przecinku.")
    private BigDecimal price;

    private boolean isSeparate;

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public boolean isSeparate() { return isSeparate; }
    public void setSeparate(boolean separate) { isSeparate = separate; }
}