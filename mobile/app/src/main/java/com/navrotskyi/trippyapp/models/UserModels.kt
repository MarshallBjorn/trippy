package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class UserResponseDto(
    val id: String,
    val name: String?,
    val email: String,
    val role: String,
    val isVerified: Boolean,
    @SerializedName("currencyCode")
    val currencyCode: String?,
    @SerializedName("photoUrl")
    val photoUrl: String?
)

data class UpdateUserRequest(
    val name: String? = null,
    val email: String? = null,
    val currentPassword: String? = null,
    val password: String? = null,
    @SerializedName("currency_code")
    val currencyCode: String? = null
)

data class CurrencyDto(
    val code: String,
    val name: String
)

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    object Success : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}