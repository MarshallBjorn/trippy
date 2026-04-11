package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path


interface TrippyApi {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Pobieranie moich wycieczek
    @GET("/api/trips/my")
    suspend fun getMyTrips(): Response<List<TripEventDto>>

    // Pobieranie listy uczestników
    @GET("/api/trips/{tripId}/participants")
    suspend fun getTripParticipants(@Path("tripId") tripId: String): Response<List<TripParticipantDto>>

    // Zapraszanie uczestnika
    @POST("/api/trips/{tripId}/participants")
    suspend fun inviteParticipant(
        @Path("tripId") tripId: String,
        @Body request: InviteParticipantRequest // Zmienione z TripParticipantDto
    ): Response<TripParticipantDto>

    @POST("/api/trips")
    suspend fun createTrip(@Body request: CreateTripEventRequest): Response<TripEventDto>

    @GET("/api/users/me")
    suspend fun getCurrentUser(): Response<UserResponseDto>

    @PUT("/api/users/me")
    suspend fun updateCurrentUser(@Body request: UpdateUserRequest): Response<UserResponseDto>

    @GET("/api/dictionaries/currencies")
    suspend fun getCurrencies(): Response<List<CurrencyDto>>

    @Multipart
    @POST("api/users/me/photo")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): Response<UserResponseDto>
}