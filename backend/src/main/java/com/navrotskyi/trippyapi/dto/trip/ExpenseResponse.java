package com.navrotskyi.trippyapi.dto.trip;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID tripId,
        String title,
        BigDecimal price,
        Boolean isSeparate
) {}