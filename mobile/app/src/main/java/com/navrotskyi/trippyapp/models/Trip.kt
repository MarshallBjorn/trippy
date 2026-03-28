package com.navrotskyi.trippyapp.models

import com.navrotskyi.trippyapp.data.entity.User

// Model używany w widokach (UI)
data class Trip(
    val id: String,
    val owner: User?,
    val name: String,
    val pickedCurrency: String,
    val startDate: String,
    val endDate: String,
    val budget: Double
)

// Model przychodzący z serwera (DTO)
data class TripEventDto(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val budget: Double,
    val currencyCode: String
)