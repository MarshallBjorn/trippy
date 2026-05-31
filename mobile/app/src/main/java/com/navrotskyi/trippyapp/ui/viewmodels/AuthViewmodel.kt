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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val token: String) : AuthState()
    data class AwaitingVerification(val email: String) : AuthState()
    data class Error(val message: String, val errors: List<String>? = null) : AuthState()
}

data class RegisterFormErrors(
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class AuthViewModel : ViewModel() {
    private val api by lazy { RetrofitClient.retrofit.create(TrippyApi::class.java) }

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#\$!%*?&])[A-Za-z\\d@#\$!%*?&]{8,128}$")

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    private val _registerErrors = MutableStateFlow(RegisterFormErrors())
    val registerErrors: StateFlow<RegisterFormErrors> = _registerErrors.asStateFlow()

    fun clearRegisterErrors() { _registerErrors.value = RegisterFormErrors() }

    fun validateRegisterForm(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true
        val nameErr = if (name.isBlank()) { isValid = false; "Imię lub nick jest wymagany" } else null
        val emailErr = when {
            email.isBlank() -> { isValid = false; "Adres e-mail jest wymagany" }
            !emailRegex.matches(email) -> { isValid = false; "Niepoprawny format e-mail" }
            else -> null
        }
        val passwordErr = when {
            password.isBlank() -> { isValid = false; "Hasło jest wymagane" }
            !passwordRegex.matches(password) -> { isValid = false; "Min. 8 znaków: wielka i mała litera, cyfra, znak specjalny (@#\$!%*?&)" }
            else -> null
        }
        val confirmPasswordErr = when {
            confirmPassword.isBlank() -> { isValid = false; "Powtórz hasło" }
            confirmPassword != password -> { isValid = false; "Hasła nie są identyczne" }
            else -> null
        }
        _registerErrors.value = RegisterFormErrors(nameErr, emailErr, passwordErr, confirmPasswordErr)
        return isValid
    }

    fun login(email: String, password: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    authState = AuthState.Success(response.body()!!.accessToken)
                }
            } catch (e: ApiException) {

                Log.e(
                    "AuthViewModel",
                    "Login API error",
                    e
                )

                authState = AuthState.Error(
                    e.message,
                    e.errors
                )

            } catch (e: Exception) {

                Log.e(
                    "AuthViewModel",
                    "Unexpected login error",
                    e
                )

                authState = AuthState.Error(
                    "Nieoczekiwany błąd: ${e.message}"
                )
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        authState = AuthState.Loading
        viewModelScope.launch {
            try {
                val response = api.register(RegisterRequest(name, email, password, confirmPassword))

                if (response.isSuccessful) {
                    authState = AuthState.AwaitingVerification(email)
                }
            } catch (e: ApiException) {

                Log.e(
                    "AuthViewModel",
                    "Register API error",
                    e
                )

                authState = AuthState.Error(
                    e.message,
                    e.errors
                )

            } catch (e: Exception) {

                Log.e(
                    "AuthViewModel",
                    "Unexpected register error",
                    e
                )

                authState = AuthState.Error(
                    "Nieoczekiwany błąd: ${e.message}"
                )
            }
        }
    }

    fun resetState() {
        authState = AuthState.Idle
    }
}