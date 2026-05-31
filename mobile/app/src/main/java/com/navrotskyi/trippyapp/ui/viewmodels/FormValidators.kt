package com.navrotskyi.trippyapp.ui.viewmodels

object FormValidators {
    private val emailRegex =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    private val strongPasswordRegex =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$!%*?&])[A-Za-z\\d@#\$!%*?&]{8,128}$".toRegex()

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isStrongPassword(password: String): Boolean = strongPasswordRegex.matches(password)
}