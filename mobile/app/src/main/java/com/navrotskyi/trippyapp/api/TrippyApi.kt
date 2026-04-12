package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

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

    //OBSŁUGA WYDARZEŃ (NODE'ÓW) 

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

    // Pobranie konkretnego wydarzenia (Szczegóły)
    @GET("/api/trips/{tripId}/nodes/{nodeId}")
    suspend fun getTripNode(
        @Path("tripId") tripId: String,
        @Path("nodeId") nodeId: String
    ): Response<TripNodeDto>

    // Edycja wydarzenia
    @PUT("/api/trips/{tripId}/nodes/{nodeId}")
    suspend fun updateTripNode(
        @Path("tripId") tripId: String,
        @Path("nodeId") nodeId: String,
        @Body request: CreateTripNodeRequest
    ): Response<TripNodeDto>

    //OBSŁUGA PROFILU I SŁOWNIKÓW (Z gałęzi develop) 

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