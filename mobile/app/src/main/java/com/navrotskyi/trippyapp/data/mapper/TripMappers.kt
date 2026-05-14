package com.navrotskyi.trippyapp.data.mapper

import com.navrotskyi.trippyapp.data.entity.TripEntity
import com.navrotskyi.trippyapp.data.entity.TripNodeEntity
import com.navrotskyi.trippyapp.models.Trip
import com.navrotskyi.trippyapp.models.TripEventDto
import com.navrotskyi.trippyapp.models.TripNodeDto

fun TripEventDto.toEntity(): TripEntity = TripEntity(
    id = id,
    ownerId = ownerId,
    name = name,
    pickedCurrency = currencyCode,
    startDate = startDate,
    endDate = endDate,
    budget = budget
)

fun TripEntity.toDomain(): Trip = Trip(
    id = id,
    ownerId = ownerId,
    owner = null,
    name = name,
    pickedCurrency = pickedCurrency,
    startDate = startDate,
    endDate = endDate,
    budget = budget
)

fun TripNodeDto.toEntity(tripId: String): TripNodeEntity = TripNodeEntity(
    id = id,
    tripId = tripId,
    reporterId = reporterId,
    reporterName = reporterName,
    startTime = startTime,
    endTime = endTime,
    name = name,
    note = note,
    price = price,
    separate = separate,
    category = category,
    canEdit = canEdit,
    canDelete = canDelete
)

fun TripNodeEntity.toDto(): TripNodeDto = TripNodeDto(
    id = id,
    eventId = tripId,
    reporterId = reporterId,
    reporterName = reporterName,
    startTime = startTime,
    endTime = endTime,
    name = name,
    note = note,
    price = price,
    separate = separate,
    category = category,
    canEdit = canEdit,
    canDelete = canDelete
)