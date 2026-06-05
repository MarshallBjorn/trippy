package com.navrotskyi.trippyapp.api

import android.util.Log
import com.navrotskyi.trippyapp.models.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.contains("/api/auth/")) return null
        if (responseCount(response) >= 2) return null

        val refreshToken = TokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) return null

        synchronized(lock) {
            val failedToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")?.trim()
            val currentToken = TokenManager.getToken()
            if (!currentToken.isNullOrEmpty() && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshResult = try {
                RetrofitClient.refreshApi.refresh(RefreshTokenRequest(refreshToken)).execute()
            } catch (e: Exception) {
                Log.e("TokenAuthenticator", "Refresh request failed", e)
                null
            }

            val body = refreshResult?.takeIf { it.isSuccessful }?.body()
            if (body != null) {
                TokenManager.saveTokens(body.accessToken, body.refreshToken)
                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${body.accessToken}")
                    .build()
            }

            Log.w("TokenAuthenticator", "Refresh failed (code=${refreshResult?.code()}), clearing session")
            TokenManager.clearToken()
            return null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }

    companion object { private val lock = Any() }
}