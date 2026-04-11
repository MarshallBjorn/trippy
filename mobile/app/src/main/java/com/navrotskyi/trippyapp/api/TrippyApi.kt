package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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


    // Pobieranie wydarzeń (Node'ów)
    @GET("/api/trips/{tripId}/nodes")
    suspend fun getTripNodes(@Path("tripId") tripId: String): Response<List<TripNodeDto>>

    // Tworzenie wydarzenia
    @POST("/api/trips/{tripId}/nodes")
    suspend fun createTripNode(
        @Path("tripId") tripId: String,
        @Body request: CreateTripNodeRequest
    ): Response<TripNodeDto>

    // Usuwanie wydarzenia
    @DELETE("/api/trips/{tripId}/nodes/{nodeId}")
    suspend fun deleteTripNode(
        @Path("tripId") tripId: String,
        @Path("nodeId") nodeId: String
    ): Response<Unit>

    // Pobranie konkretnej wycieczki
    @GET("/api/trips/{tripId}/nodes/{nodeId}")
    suspend fun getTripNode(
        @Path("tripId") tripId: String,
        @Path("nodeId") nodeId: String
    ): Response<TripNodeDto>

    @PUT("/api/trips/{tripId}/nodes/{nodeId}")
    suspend fun updateTripNode(
        @Path("tripId") tripId: String,
        @Path("nodeId") nodeId: String,
        @Body request: CreateTripNodeRequest
    ): Response<TripNodeDto>
}