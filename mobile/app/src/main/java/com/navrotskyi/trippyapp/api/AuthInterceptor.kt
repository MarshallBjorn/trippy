package com.navrotskyi.trippyapp.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // Sprawdzamy, czy zapytanie nie dotyczy logowania lub rejestracji
        val path = originalRequest.url.encodedPath
        val isAuthEndpoint = path.contains("/api/auth/login") || path.contains("/api/auth/register")

        if (!isAuthEndpoint) {
            // Pobieranie zapisanego tokenu
            val token = TokenManager.getToken()

            // Jeśli token istnieje, doklejenie go do nagłówka zapytania
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        return chain.proceed(requestBuilder.build())
    }
}