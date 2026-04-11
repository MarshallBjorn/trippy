package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class InviteParticipantRequest(
    @SerializedName("userEmail")
    val email: String,

    @SerializedName("roleName")
    val role: String = "PARTICIPANT"
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

data class CreateTripEventRequest(
    val name: String,
    val currencyCode: String,
    val startDate: String,
    val endDate: String,
    val budget: Double
)

data class TripNodeDto(
    val id: String,
    val eventId: String,
    val reporterId: String,
    val reporterName: String,
    val startTime: String,
    val endTime: String,
    val name: String,
    val note: String?,
    val price: Double,
    @SerializedName("separate")
    val isSeparate: Boolean
)

data class CreateTripNodeRequest(
    val name: String,
    val startTime: String,
    val endTime: String,
    val note: String?,
    val price: Double,
    val isSeparate: Boolean
)