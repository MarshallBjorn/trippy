package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.LoginRequest
import com.navrotskyi.trippyapp.models.RegisterRequest
import com.navrotskyi.trippyapp.models.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface TrippyApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
}