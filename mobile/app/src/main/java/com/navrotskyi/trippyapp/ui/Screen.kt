package com.navrotskyi.trippyapp.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    //profil
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Currency : Screen("currency")
    object ChangePassword : Screen("change_password")

    //wycieczki
    object Trips : Screen("trips")
    object TripDetails : Screen("trip_details/{tripId}") {
        fun createRoute(tripId: String) = "trip_details/$tripId"
    }

    object InviteParticipant : Screen("invite_participant/{tripId}") {
        fun createRoute(tripId: String) = "invite_participant/$tripId"
    }

    //wydatki
    object Expenses : Screen("expenses")
}
