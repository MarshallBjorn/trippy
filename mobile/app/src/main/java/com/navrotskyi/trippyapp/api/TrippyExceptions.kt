package com.navrotskyi.trippyapp.api

import java.io.IOException


class ApiException(val code: Int, override val message: String, val errors: List<String>? = null) : IOException(message)

class NoInternetException(
    override val message: String = "Brak połączenia z internetem. Sprawdź swoją sieć i spróbuj ponownie."
) : IOException(message)