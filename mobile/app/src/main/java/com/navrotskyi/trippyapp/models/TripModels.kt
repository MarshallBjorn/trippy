package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName
import com.navrotskyi.trippyapp.data.entity.User

data class Trip(
    val id: String,
    val ownerId: String,
    val owner: User?,
    val name: String,
    val pickedCurrency: String,
    val startDate: String,
    val endDate: String,
    val budget: Double
)

data class TripEventDto(
    val id: String,
    val ownerId: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val budget: Double,
    val currencyCode: String

)

data class CreateTripEventRequest(
    val name: String,
    val currencyCode: String,
    val startDate: String,
    val endDate: String,
    val budget: Double
)

data class InviteParticipantRequest(
    @SerializedName("userEmail")
    val email: String,
    @SerializedName("roleName")
    val role: String = "Participant"
)

data class TripParticipantDto(
    val id: String? = null,
    val userId: String? = null,
    val userName: String? = null,
    val userPhotoUrl: String? = null,
    val eventId: String? = null,
    val tripRole: String? = null,
    val personalBudget: Double? = null,
    @SerializedName("accepted")
    val isAccepted: Boolean = false
)

data class TripNodeDto(
    val id: String,
    val eventId: String? = null,
    val reporterId: String? = null,
    val reporterName: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val name: String,
    val note: String? = null,
    val price: Double = 0.0,
    @SerializedName("separate")
    val separate: Boolean = false,
    val category: String? = "Inne",
    val canEdit: Boolean,
    val canDelete: Boolean
)

data class CreateTripNodeRequest(
    val name: String,
    val startTime: String,
    val endTime: String,
    val note: String?,
    val price: Double,

    @SerializedName("isSeparate")
    val separate: Boolean
)
data class SettlementDto(
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double
)

data class PostPhotoDto(
    val id: String,
    val photoUrl: String?
)

data class TripPostDto(
    val id: String,
    val note: String,
    val reporterName: String,
    val photos: List<PostPhotoDto> = emptyList()
)

data class ExpensesSummaryDto(
    val tripId: String,

    @SerializedName("currencyCode")
    val currency: String,

    @SerializedName("totalSharedExpenses")
    val totalSpent: Double,

    val balances: List<BalanceDto>,

    @SerializedName("settlements")
    val pendingSettlements: List<SettlementDto>
)

data class BalanceDto(
    val userId: String,
    val userName: String,
    val netBalance: Double
)