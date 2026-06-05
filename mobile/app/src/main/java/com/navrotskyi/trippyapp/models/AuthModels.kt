package com.navrotskyi.trippyapp.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirmPassword")
    val confirmPassword: String
)

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)