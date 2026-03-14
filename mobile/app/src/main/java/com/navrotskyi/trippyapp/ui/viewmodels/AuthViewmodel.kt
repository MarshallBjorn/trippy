package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.api.TokenManager
import com.navrotskyi.trippyapp.models.LoginRequest    // Import z models
import com.navrotskyi.trippyapp.models.RegisterRequest // Import z models
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    fun login(email: String, password: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    authState = AuthState.Success(response.body()!!.token)
                } else {
                    authState = AuthState.Error("Błędny e-mail lub hasło")
                }
            } catch (e: Exception) {
                authState = AuthState.Error("Błąd serwera: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        authState = AuthState.Loading // Dodajemy stan ładowania
        viewModelScope.launch {
            try {
                // Zmieniamy 'repository' na 'api'
                val response = api.register(RegisterRequest(name, email, password))

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    TokenManager.saveToken(token)
                    authState = AuthState.Success(token) // Poprawione przypisanie stanu
                } else {
                    authState = AuthState.Error("Błąd rejestracji: ${response.code()}")
                }
            } catch (e: Exception) {
                authState = AuthState.Error("Brak połączenia: ${e.message}")
            }
        }
    }

    fun resetState() {
        authState = AuthState.Idle
    }
}