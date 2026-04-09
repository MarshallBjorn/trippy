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
    val category: String? = "Inne"
)

data class SettlementDto(
    val id: String,
    val fromUserName: String,
    val toUserName: String,
    val amount: Double,
    val currency: String,
    val tripName: String,
    val date: String,
    val isIncoming: Boolean
)

data class ExpensesSummaryDto(
    val totalSpent: Double,
    val currency: String,
    val categoryBreakdown: Map<String, Double>,
    val userBalance: Double,
    val pendingSettlements: List<SettlementDto>
)
