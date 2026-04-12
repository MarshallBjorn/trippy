package com.navrotskyi.trippyapp.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")

    //profil
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Currency : Screen("currency")
    object ChangePassword : Screen("change_password")

    object Trips : Screen("trips")
    object TripDetails : Screen("trip_details/{tripId}") {
        fun createRoute(tripId: String) = "trip_details/$tripId"
    }

    object InviteParticipant : Screen("invite_participant/{tripId}") {
        fun createRoute(tripId: String) = "invite_participant/$tripId"
    }

    object AddTrip : Screen("add_trip")

    object Expenses : Screen("expenses")

    object AddNode : Screen("add_node/{tripId}") {
        fun createRoute(tripId: String) = "add_node/$tripId"
    }


    object NodeDetails : Screen("node_details/{tripId}/{nodeId}") {
        fun createRoute(tripId: String, nodeId: String) = "node_details/$tripId/$nodeId"
    }

    object EditNode : Screen("edit_node/{tripId}/{nodeId}") {
        fun createRoute(tripId: String, nodeId: String) = "edit_node/$tripId/$nodeId"
    }
}
