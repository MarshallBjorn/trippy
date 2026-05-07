package com.navrotskyi.trippyapp.api

import com.google.gson.Gson
import com.navrotskyi.trippyapp.models.ErrorResponseDto
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

class GlobalErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response: Response

        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            when (e) {
                is UnknownHostException, is ConnectException -> throw NoInternetException()
                else -> throw IOException("Nieoczekiwany błąd sieci: ${e.message}")
            }
        }

        if (!response.isSuccessful) {
            val errorBodyString = response.body?.string()

            var errors: List<String>? = null
            var errorMessage = "Wystąpił nieznany błąd po stronie serwera."
            var errorCode = response.code

            try {
                if (!errorBodyString.isNullOrEmpty()) {
                    val errorDto = Gson().fromJson(errorBodyString, ErrorResponseDto::class.java)
                    errorMessage = errorDto.message ?: errorMessage
                    errorCode = errorDto.status ?: errorCode
                    errors = errorDto.errors
                }
            } catch (e: Exception) {
            }

            throw ApiException(errorCode, errorMessage, errors)
        }

        return response
    }
}