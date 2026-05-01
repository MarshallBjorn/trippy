package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class ErrorResponseDto(
    @SerializedName("status") val status: Int? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("errors") val errors: List<String>? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)