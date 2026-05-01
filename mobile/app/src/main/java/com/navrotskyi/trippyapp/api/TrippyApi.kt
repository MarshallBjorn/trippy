package com.navrotskyi.trippyapp.api

import com.navrotskyi.trippyapp.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    // @GET("/api/trips/{tripId}/balances")
    // suspend fun getGroupBalances(@Path("tripId") tripId: String): Response<GroupBalancesDto>

    //obsluga zaproszen
    @GET("/api/users/me/invitations")
    suspend fun getInvitations(): Response<List<InvitationDto>>

    @POST("/api/trips/{id}/accept")
    suspend fun acceptInvitation(@Path("id") id: String): Response<Unit>

    @POST("/api/trips/{id}/reject")
    suspend fun rejectInvitation(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("api/users/me/photo")
    suspend fun uploadProfilePhoto(
        @Part file: MultipartBody.Part
    ): Response<UserResponseDto>

    @GET("/api/posts/node/{nodeId}")
    suspend fun getPostsByNode(@Path("nodeId") nodeId: String): Response<List<TripPostDto>>

    @Multipart
    @POST("/api/posts")
    suspend fun createPost(
        @Part("data") data: RequestBody,
        @Part photos: List<MultipartBody.Part>
    ): Response<TripPostDto>
}