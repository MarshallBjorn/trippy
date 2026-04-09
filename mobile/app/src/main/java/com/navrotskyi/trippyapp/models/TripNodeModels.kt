package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class TripNodeDto(
    val id: String,
    val eventId: String,
    val reporterId: String,
    val start: String,
    val end: String,
    val name: String,
    val note: String?,
    val price: Double,
    @SerializedName("separate")
    val isSeparate: Boolean,
    val category: String? = "Inne" // Adding category as it's in the mockup
)

data class SettlementDto(
    val id: String,
    val fromUserName: String,
    val toUserName: String,
    val amount: Double,
    val currency: String,
    val tripName: String,
    val date: String,
    val isIncoming: Boolean // To distinguish "Oddajesz" vs "Otrzymujesz"
)

data class ExpensesSummaryDto(
    val totalSpent: Double,
    val currency: String,
    val categoryBreakdown: Map<String, Double>,
    val userBalance: Double, // positive if others owe me, negative if I owe
    val pendingSettlements: List<SettlementDto>
)
