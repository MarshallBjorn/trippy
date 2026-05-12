package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.CurrencyDto
import com.navrotskyi.trippyapp.models.ProfileUiState
import com.navrotskyi.trippyapp.models.UpdateUserRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

data class ChangePasswordFormErrors(
    val oldPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null
)

class ProfileViewModel(private val userDao: UserDao, private val api: TrippyApi = RetrofitClient.retrofit.create(TrippyApi::class.java) ) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    private val _currencies = MutableStateFlow<List<CurrencyDto>>(emptyList())
    val currencies: StateFlow<List<CurrencyDto>> = _currencies.asStateFlow()
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _changePasswordErrors = MutableStateFlow(ChangePasswordFormErrors())
    val changePasswordErrors: StateFlow<ChangePasswordFormErrors> = _changePasswordErrors.asStateFlow()

    init {
        observeUserData()
        fetchCurrencies()
        fetchUserFromApi()
    }

    private fun observeUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.getAll().collect { users ->
                _user.value = users.firstOrNull()
            }
        }
    }
    fun fetchUserFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ProfileUiState.Loading
            try {
                val response = api.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    val userDto = response.body()!!
                    val existingUser = userDao.getAll().firstOrNull()?.firstOrNull()
                    val rawUrl = userDto.photoUrl
                    val formattedUrl = rawUrl?.replace("localhost", "10.0.2.2")

                    val userToSave = existingUser?.copy(
                        name = userDto.name ?: existingUser.name,
                        email = userDto.email,
                        role = userDto.role,
                        stringUrl = formattedUrl,
                        currency = userDto.currencyCode ?: existingUser.currency,
                        isVerified = userDto.isVerified
                    ) ?: User(
                        name = userDto.name,
                        email = userDto.email,
                        role = userDto.role,
                        stringUrl = formattedUrl,
                        isVerified = userDto.isVerified,
                        currency = userDto.currencyCode ?: "PLN"
                    )

                    userDao.insertAll(listOf(userToSave))
                    _uiState.value = ProfileUiState.Success
                } else {
                    _uiState.value = ProfileUiState.Error("Nie udało się pobrać danych profilu (Błąd: ${response.code()})")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Błąd fetchUserFromApi", e)
                _uiState.value = ProfileUiState.Error("Brak połączenia z serwerem. Sprawdź internet.")
            }
        }
    }
    fun fetchCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.getCurrencies()

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null) {
                        _currencies.value = body
                    } else {
                        android.util.Log.e(
                            "ProfileVM",
                            "fetchCurrencies: response body is null"
                        )
                    }
                    _uiState.value = ProfileUiState.Success
                } else {
                    android.util.Log.e(
                        "ProfileVM",
                        "fetchCurrencies: error code ${response.code()}"
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Błąd fetchCurrencies", e)
            }
        }
    }
    fun updateUserName(newName: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ProfileUiState.Loading
            try {
                val request = UpdateUserRequest(name = newName)
                val response = api.updateCurrentUser(request)

                if (response.isSuccessful) {
                    _user.value?.let { currentUser ->
                        val updatedUser = currentUser.copy(name = newName)
                        userDao.update(listOf(updatedUser))
                        _user.value = updatedUser
                    }
                    _uiState.value = ProfileUiState.Success
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    _uiState.value = ProfileUiState.Error("Serwer odrzucił zmianę nazwy.")
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Błąd updateUserName", e)
                _uiState.value = ProfileUiState.Error("Błąd sieci. Nie zapisano zmian.")
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
    fun updateCurrency(newCurrencyCode: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ProfileUiState.Loading
            try {
                val request = UpdateUserRequest(currencyCode = newCurrencyCode)
                val response = api.updateCurrentUser(request)

                if (response.isSuccessful) {
                    _user.value?.let { currentUser ->
                        val updatedUser = currentUser.copy(currency = newCurrencyCode)
                        userDao.update(listOf(updatedUser))
                        _user.value = updatedUser
                    }
                    _uiState.value = ProfileUiState.Success
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    _uiState.value = ProfileUiState.Error("Nie udało się zaktualizować waluty.")
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Błąd updateCurrency", e)
                _uiState.value = ProfileUiState.Error("Błąd sieci. Waluta nie została zmieniona.")
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
    fun updatePassword(oldPass: String, newPass: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ProfileUiState.Loading
            try {
                val request = UpdateUserRequest(currentPassword = oldPass, password = newPass)
                val response = api.updateCurrentUser(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        _uiState.value = ProfileUiState.Success
                        onComplete(true)
                    } else {
                        _uiState.value = ProfileUiState.Error("Obecne hasło jest nieprawidłowe.")
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Błąd updatePassword", e)
                withContext(Dispatchers.Main) {
                    _uiState.value = ProfileUiState.Error("Błąd sieci. Sprawdź połączenie.")
                    onComplete(false)
                }
            }
        }
    }
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.deleteAll()

            _user.value = null
            _uiState.value = ProfileUiState.Idle
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }
    fun resetError() {
        _uiState.value = ProfileUiState.Idle
    }
    fun uploadProfileImage(uri: Uri, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ProfileUiState.Loading
            try {
                val file = copyUriToFile(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = api.uploadProfilePhoto(body)

                if (response.isSuccessful) {
                    fetchUserFromApi()
                    _uiState.value = ProfileUiState.Success
                } else {
                    _uiState.value = ProfileUiState.Error("Nie udało się przesłać zdjęcia (Błąd: ${response.code()})")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Błąd uploadu", e)
                _uiState.value = ProfileUiState.Error("Błąd sieci podczas wysyłania zdjęcia.")
            }
        }
    }
    private fun copyUriToFile(context: android.content.Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "profile_upload_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    fun validateChangePasswordForm(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        var oldErr: String? = null
        var newErr: String? = null
        var confirmErr: String? = null

        if (oldPassword.isBlank()) {
            oldErr = "Wprowadź obecne hasło"; isValid = false
        }

        when {
            newPassword.isBlank() -> {
                newErr = "Wprowadź nowe hasło"; isValid = false
            }
            newPassword == oldPassword -> {
                newErr = "Nowe hasło musi być inne niż obecne"; isValid = false
            }
            !FormValidators.isStrongPassword(newPassword) -> {
                newErr = "Min. 8 znaków: wielka, mała litera, cyfra i znak specjalny"
                isValid = false
            }
        }

        when {
            confirmPassword.isBlank() -> {
                confirmErr = "Powtórz nowe hasło"; isValid = false
            }
            newPassword != confirmPassword -> {
                confirmErr = "Hasła nie są identyczne"; isValid = false
            }
        }

        _changePasswordErrors.value = ChangePasswordFormErrors(oldErr, newErr, confirmErr)
        return isValid
    }

    fun clearChangePasswordErrors() {
        _changePasswordErrors.value = ChangePasswordFormErrors()
    }
}
class ProfileViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
