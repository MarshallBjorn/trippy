package com.navrotskyi.trippyapi.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripNodeDto(
        UUID id,
        UUID eventId,
        UUID reporterId,
        String reporterName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String name,
        String note,
        BigDecimal price,
        boolean separate,
        boolean canEdit,
        boolean canDelete
) {}