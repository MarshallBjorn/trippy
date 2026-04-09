package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
        @Body request: InviteParticipantRequest
    ): Response<TripParticipantDto>

    @POST("/api/trips")
    suspend fun createTrip(@Body request: CreateTripEventRequest): Response<TripEventDto>

    // Pobieranie wydatków (nodes) danej wycieczki
    @GET("/api/trips/{eventId}/nodes")
    suspend fun getTripNodes(@Path("eventId") eventId: String): Response<List<TripNodeDto>>
}
