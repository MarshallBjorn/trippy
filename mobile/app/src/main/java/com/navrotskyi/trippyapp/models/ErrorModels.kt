package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class ErrorResponseDto(
    @SerializedName("status") val status: Int,
    @SerializedName("error") val error: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String
)