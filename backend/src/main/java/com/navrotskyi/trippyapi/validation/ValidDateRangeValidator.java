package com.navrotskyi.trippyapi.validation;

import com.navrotskyi.trippyapi.dto.CreateTripEventRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, CreateTripEventRequest> {
    @Override
    public boolean isValid(CreateTripEventRequest r, ConstraintValidatorContext ctx) {
        if (r.startDate() == null || r.endDate() == null) return true; // @NotNull handles null
        return !r.endDate().isBefore(r.startDate());
    }
}