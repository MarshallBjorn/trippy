package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class GroupBalancesDto(
    @SerializedName("tripId") val tripId: String,
    @SerializedName("currencyCode") val currencyCode: String,
    @SerializedName("totalSharedExpenses") val totalSharedExpenses: Double,
    @SerializedName("balances") val balances: List<ParticipantBalanceDto>,
    @SerializedName("settlements") val settlements: List<GroupSettlementDto>
)

data class ParticipantBalanceDto(
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("netBalance") val netBalance: Double
)

data class GroupSettlementDto(
    @SerializedName("fromUserId") val fromUserId: String,
    @SerializedName("fromUserName") val fromUserName: String,
    @SerializedName("toUserId") val toUserId: String,
    @SerializedName("toUserName") val toUserName: String,
    @SerializedName("amount") val amount: Double
)