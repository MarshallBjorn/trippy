package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class GroupBalancesDto(
    @SerializedName("totalBalance") val totalBalance: Double,
    @SerializedName("participants") val participants: List<ParticipantBalanceDto>,


    @SerializedName("settlements") val settlements: List<GroupSettlementDto>
)

data class ParticipantBalanceDto(
    @SerializedName("userName") val userName: String,
    @SerializedName("balance") val balance: Double,
    @SerializedName("initials") val initials: String,
    @SerializedName("avatarUrl") val avatarUrl: String? = null
)

data class GroupSettlementDto(
    @SerializedName("otherPersonName") val otherPersonName: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("type") val type: String
)