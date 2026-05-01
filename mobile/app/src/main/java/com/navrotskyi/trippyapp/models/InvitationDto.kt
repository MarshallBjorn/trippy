package com.navrotskyi.trippyapp.models

data class InvitationDto(
    val tripId: String,
    val tripName: String,
    val inviterName: String,
    val role: String,
    val accepted: Boolean
)