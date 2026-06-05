package com.navrotskyi.trippyapp.api

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val KEY_ACCESS = "JWT_TOKEN"
    private const val KEY_REFRESH = "REFRESH_TOKEN"

    private var prefs: SharedPreferences? = null

    // Inicjalizacja
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("trippy_auth", Context.MODE_PRIVATE)
    }

    // Zapisywanie tokena
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs?.edit()
            ?.putString(KEY_ACCESS, accessToken)
            ?.putString(KEY_REFRESH, refreshToken)
            ?.apply()
    }

    fun saveToken(token: String) {
        prefs?.edit()
            ?.putString(KEY_ACCESS, token)
            ?.apply()
    }

    // Pobieranie tokena
    fun getToken(): String? {
        return prefs?.getString(KEY_ACCESS, null)
    }

    fun getRefreshToken(): String? {
        return prefs?.getString(KEY_REFRESH, null)
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clearToken() {
        prefs?.edit()?.remove(KEY_ACCESS)?.remove(KEY_REFRESH)?.apply()
    }
}