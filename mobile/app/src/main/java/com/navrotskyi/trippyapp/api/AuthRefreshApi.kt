package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.AuthResponse
import com.navrotskyi.trippyapp.models.RefreshTokenRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthRefreshApi {
    @POST("/api/auth/refresh")
    fun refresh(@Body request: RefreshTokenRequest): Call<AuthResponse>
}