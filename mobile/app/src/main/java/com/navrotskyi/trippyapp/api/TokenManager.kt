package com.navrotskyi.trippyapp.api

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private var prefs: SharedPreferences? = null

    // Inicjalizacja
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("trippy_auth", Context.MODE_PRIVATE)
    }

    // Zapisywanie tokena
    fun saveToken(token: String) {
        prefs?.edit()?.putString("JWT_TOKEN", token)?.apply()
    }

    // Pobieranie tokena
    fun getToken(): String? {
        return prefs?.getString("JWT_TOKEN", null)
    }

    // Wylogowanie (czyszczenie tokena)
    fun clearToken() {
        prefs?.edit()?.remove("JWT_TOKEN")?.apply()
    }
}