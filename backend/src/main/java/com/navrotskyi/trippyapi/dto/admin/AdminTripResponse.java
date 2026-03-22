package com.navrotskyi.trippyapi.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AdminTripResponse(
        UUID id,
        String name,
        String ownerName,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        String currencyCode
) {}