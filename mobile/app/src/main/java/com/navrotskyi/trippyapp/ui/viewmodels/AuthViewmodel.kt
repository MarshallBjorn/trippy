package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.ApiException
import com.navrotskyi.trippyapp.api.NoInternetException
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.api.TokenManager
import com.navrotskyi.trippyapp.models.LoginRequest
import com.navrotskyi.trippyapp.models.RegisterRequest
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class AwaitingVerification(val email: String) : AuthState()
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
                if (response.isSuccessful) {
                    // Update: use accessToken from the response
                    authState = AuthState.Success(response.body()!!.accessToken)
                }
            } catch (e: ApiException) {
                authState = AuthState.Error(e.message)
            } catch (e: Exception) {
                authState = AuthState.Error("Nieoczekiwany błąd: ${e.message}")
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.register(RegisterRequest(name, email, password, confirmPassword))

                if (response.isSuccessful) {
                    // Do NOT save token - user is unverified
                    authState = AuthState.AwaitingVerification(email)
                }
            } catch (e: ApiException) {
                authState = AuthState.Error(e.message)
            } catch (e: Exception) {
                authState = AuthState.Error("Nieoczekiwany błąd: ${e.message}")
            }
        }
    }

    fun resetState() {
        authState = AuthState.Idle
    }
}