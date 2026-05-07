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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RegisterFormErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class AwaitingVerification(val email: String) : AuthState()
    data class Error(val message: String, val errors: List<String>? = null) : AuthState()
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
                authState = AuthState.Error(e.message, e.errors)
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
                authState = AuthState.Error(e.message, e.errors)
            } catch (e: Exception) {
                authState = AuthState.Error("Nieoczekiwany błąd: ${e.message}")
            }
        }
    }

    fun resetState() {
        authState = AuthState.Idle
    }

    private val _registerErrors = MutableStateFlow(RegisterFormErrors())
    val registerErrors: StateFlow<RegisterFormErrors> = _registerErrors.asStateFlow()

    private val emailRegex =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    private val strongPasswordRegex =
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$!%*?&])[A-Za-z\\d@#\$!%*?&]{8,128}$".toRegex()

    fun validateRegisterForm(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        var nameErr: String? = null
        var emailErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null

        if (name.isBlank()) {
            nameErr = "Imię/nick jest wymagane"; isValid = false
        } else if (name.length < 2) {
            nameErr = "Imię musi mieć co najmniej 2 znaki"; isValid = false
        }

        if (email.isBlank()) {
            emailErr = "E-mail jest wymagany"; isValid = false
        } else if (!emailRegex.matches(email.trim())) {
            emailErr = "Niepoprawny format e-mail"; isValid = false
        }

        if (password.isBlank()) {
            passErr = "Hasło jest wymagane"; isValid = false
        } else if (!strongPasswordRegex.matches(password)) {
            passErr = "Min. 8 znaków: wielka, mała litera, cyfra i znak specjalny"
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            confirmErr = "Powtórz hasło"; isValid = false
        } else if (password != confirmPassword) {
            confirmErr = "Hasła nie są identyczne"; isValid = false
        }

        _registerErrors.value =
            RegisterFormErrors(nameErr, emailErr, passErr, confirmErr)
        return isValid
    }

    fun clearRegisterErrors() {
        _registerErrors.value = RegisterFormErrors()
    }
}