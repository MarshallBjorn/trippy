package com.navrotskyi.trippyapp.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
}