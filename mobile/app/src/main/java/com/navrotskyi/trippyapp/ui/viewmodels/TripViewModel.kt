package com.navrotskyi.trippyapp.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// FORM ERRORS

data class AddTripFormErrors(
    val nameError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
    val budgetError: String? = null
)

data class TripNodeFormErrors(
    val nameError: String? = null,
    val startTimeError: String? = null,
    val endTimeError: String? = null,
    val priceError: String? = null
)

data class InviteFormErrors(
    val emailError: String? = null
)

// STATES

sealed class CreateTripState {
    object Idle : CreateTripState()
    object Loading : CreateTripState()
    object Success : CreateTripState()
    data class Error(val message: String) : CreateTripState()
}

sealed class InviteState {
    object Idle : InviteState()
    object Loading : InviteState()
    object Success : InviteState()
    data class Error(val message: String) : InviteState()
}

sealed class CreateTripNodeState {
    object Idle : CreateTripNodeState()
    object Loading : CreateTripNodeState()
    object Success : CreateTripNodeState()
    data class Error(val message: String) : CreateTripNodeState()
}

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

class TripViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)

    private val emailRegex =
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _participants = MutableStateFlow<List<TripParticipantDto>>(emptyList())
    val participants: StateFlow<List<TripParticipantDto>> = _participants.asStateFlow()

    private val _nodes = MutableStateFlow<List<TripNodeDto>>(emptyList())
    val nodes: StateFlow<List<TripNodeDto>> = _nodes.asStateFlow()

    val expenses: StateFlow<List<TripNodeDto>> = _nodes.asStateFlow()

    private val _selectedNode = MutableStateFlow<TripNodeDto?>(null)
    val selectedNode: StateFlow<TripNodeDto?> = _selectedNode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // form errors

    private val _addTripErrors = MutableStateFlow(AddTripFormErrors())
    val addTripErrors: StateFlow<AddTripFormErrors> = _addTripErrors.asStateFlow()

    private val _nodeFormErrors = MutableStateFlow(TripNodeFormErrors())
    val nodeFormErrors: StateFlow<TripNodeFormErrors> = _nodeFormErrors.asStateFlow()

    private val _inviteFormErrors = MutableStateFlow(InviteFormErrors())
    val inviteFormErrors: StateFlow<InviteFormErrors> = _inviteFormErrors.asStateFlow()

    // action states

    private val _createTripState = MutableStateFlow<CreateTripState>(CreateTripState.Idle)
    val createTripState: StateFlow<CreateTripState> = _createTripState.asStateFlow()

    private val _createNodeState = MutableStateFlow<CreateTripNodeState>(CreateTripNodeState.Idle)
    val createNodeState: StateFlow<CreateTripNodeState> = _createNodeState.asStateFlow()

    private val _inviteState = MutableStateFlow<InviteState>(InviteState.Idle)
    val inviteState: StateFlow<InviteState> = _inviteState.asStateFlow()

    private val _posts = MutableStateFlow<List<TripPostDto>>(emptyList())
    val posts: StateFlow<List<TripPostDto>> = _posts.asStateFlow()

    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState: StateFlow<CreatePostState> = _createPostState.asStateFlow()

    init {
        loadTrips()
    }

    // WYCIECZKI

    fun loadTrips() {
        viewModelScope.launch {
            try {
                val response = api.getMyTrips()
                if (response.isSuccessful && response.body() != null) {
                    _trips.value = response.body()!!.map { dto ->
                        Trip(
                            id = dto.id,
                            owner = null,
                            name = dto.name,
                            pickedCurrency = dto.currencyCode,
                            startDate = dto.startDate,
                            endDate = dto.endDate,
                            budget = dto.budget
                        )
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun refreshTrips() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadTrips()
            _isRefreshing.value = false
        }
    }

    fun createTrip(name: String, startDate: String, endDate: String, budget: String, currency: String) {
        _createTripState.value = CreateTripState.Loading
        viewModelScope.launch {
            try {
                val request = CreateTripEventRequest(
                    name = name,
                    currencyCode = currency,
                    startDate = formatDateForApi(startDate),
                    endDate = formatDateForApi(endDate),
                    budget = budget.toDoubleOrNull() ?: 0.0
                )
                val response = api.createTrip(request)
                if (response.isSuccessful) {
                    _createTripState.value = CreateTripState.Success
                    loadTrips()
                } else {
                    _createTripState.value = CreateTripState.Error("Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                _createTripState.value = CreateTripState.Error("Brak połączenia: ${e.localizedMessage}")
            }
        }
    }

    // UCZESTNICY

    fun loadParticipants(tripId: String) {
        viewModelScope.launch {
            try {
                val response = api.getTripParticipants(tripId)
                if (response.isSuccessful && response.body() != null) {
                    _participants.value = response.body()!!
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun inviteParticipant(tripId: String, email: String) {
        _inviteState.value = InviteState.Loading
        viewModelScope.launch {
            try {
                val response = api.inviteParticipant(tripId, InviteParticipantRequest(email = email))
                if (response.isSuccessful) {
                    _inviteState.value = InviteState.Success
                    loadParticipants(tripId)
                } else {
                    _inviteState.value = InviteState.Error("Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                _inviteState.value = InviteState.Error("Brak połączenia: ${e.localizedMessage}")
            }
        }
    }

    // NODES

    fun loadNodes(tripId: String) {
        viewModelScope.launch {
            try {
                val response = api.getTripNodes(tripId)
                if (response.isSuccessful && response.body() != null) {
                    _nodes.value = response.body()!!
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun loadNode(tripId: String, nodeId: String) {
        viewModelScope.launch {
            try {
                val response = api.getTripNode(tripId, nodeId)
                if (response.isSuccessful) { _selectedNode.value = response.body() }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun createTripNode(tripId: String, name: String, startTime: String, endTime: String, price: String, note: String, separate: Boolean, category: String = "Inne") {
        _createNodeState.value = CreateTripNodeState.Loading

        val apiStart = formatDateTimeForApi(startTime)
        val apiEnd = formatDateTimeForApi(endTime)

        viewModelScope.launch {
            try {
                val formattedPrice = price.replace(",", ".").toDoubleOrNull() ?: 0.0

                val request = CreateTripNodeRequest(
                    name = name,
                    startTime = apiStart,
                    endTime = apiEnd,
                    note = note.ifBlank { null },
                    price = formattedPrice,
                    separate = separate,
                    category = category
                )
                val response = api.createTripNode(tripId, request)
                if (response.isSuccessful) {
                    _createNodeState.value = CreateTripNodeState.Success
                    loadNodes(tripId)
                } else {
                    _createNodeState.value = CreateTripNodeState.Error("Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                _createNodeState.value = CreateTripNodeState.Error("Brak połączenia: ${e.localizedMessage}")
            }
        }
    }

    fun updateTripNode(tripId: String, nodeId: String, name: String, startTime: String, endTime: String, price: String, note: String, separate: Boolean, category: String) {
        _createNodeState.value = CreateTripNodeState.Loading

        val apiStart = formatDateTimeForApi(startTime)
        val apiEnd = formatDateTimeForApi(endTime)

        viewModelScope.launch {
            try {
                val formattedPrice = price.replace(",", ".").toDoubleOrNull() ?: 0.0

                val request = CreateTripNodeRequest(
                    name = name,
                    startTime = apiStart,
                    endTime = apiEnd,
                    note = note.ifBlank { null },
                    price = formattedPrice,
                    separate = separate,
                    category = category
                )

                val response = api.updateTripNode(tripId, nodeId, request)
                if (response.isSuccessful) {
                    _createNodeState.value = CreateTripNodeState.Success
                    loadNodes(tripId)
                    loadNode(tripId, nodeId)
                } else {
                    _createNodeState.value = CreateTripNodeState.Error("Błąd serwera: ${response.code()}")
                }
            } catch (e: Exception) {
                _createNodeState.value = CreateTripNodeState.Error("Błąd połączenia: ${e.localizedMessage}")
            }
        }
    }

    fun deleteTripNode(tripId: String, nodeId: String) {
        viewModelScope.launch {
            try {
                val response = api.deleteTripNode(tripId, nodeId)
                if (response.isSuccessful) { loadNodes(tripId) }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // POSTS

    fun loadPosts(nodeId: String) {
        viewModelScope.launch {
            try {
                val response = api.getPostsByNode(nodeId)
                if (response.isSuccessful) {
                    _posts.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun createPost(nodeId: String, note: String, photoUris: List<Uri>, context: Context) {
        _createPostState.value = CreatePostState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = Gson().toJson(mapOf("nodeId" to nodeId, "note" to note))
                val data = json.toRequestBody("application/json".toMediaTypeOrNull())

                val parts = photoUris.mapIndexed { index, uri ->
                    val file = copyUriToTempFile(context, uri, index)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("photos", file.name, requestFile)
                }

                val response = api.createPost(data, parts)
                if (response.isSuccessful) {
                    _createPostState.value = CreatePostState.Success
                    loadPosts(nodeId)
                } else {
                    val msg = parseApiError(response.errorBody()?.string(), response.code())
                    _createPostState.value = CreatePostState.Error(msg)
                }
            } catch (e: Exception) {
                _createPostState.value = CreatePostState.Error("Błąd sieci: ${e.localizedMessage}")
            }
        }
    }

    fun resetCreatePostState() { _createPostState.value = CreatePostState.Idle }

    private fun parseApiError(errorBody: String?, code: Int): String {
        if (errorBody == null) return "Błąd serwera: $code"
        return try {
            val map = Gson().fromJson(errorBody, Map::class.java)
            map["message"] as? String ?: "Błąd serwera: $code"
        } catch (e: Exception) {
            "Błąd serwera: $code"
        }
    }

    private fun copyUriToTempFile(context: Context, uri: Uri, index: Int = 0): File {
        val tempFile = File(context.cacheDir, "post_photo_${System.currentTimeMillis()}_$index.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        }
        return tempFile
    }

    // RESETY

    fun resetCreateTripState() { _createTripState.value = CreateTripState.Idle }
    fun resetCreateNodeState() { _createNodeState.value = CreateTripNodeState.Idle }
    fun resetCreateTripNodeState() { _createNodeState.value = CreateTripNodeState.Idle }
    fun resetInviteState() { _inviteState.value = InviteState.Idle }
    fun clearAddTripErrors() { _addTripErrors.value = AddTripFormErrors() }
    fun clearNodeFormErrors() { _nodeFormErrors.value = TripNodeFormErrors() }
    fun clearInviteFormErrors() { _inviteFormErrors.value = InviteFormErrors() }
    fun clearSelectedNode() { _selectedNode.value = null }
    fun clearData() {
        _trips.value = emptyList()
        _participants.value = emptyList()
        _nodes.value = emptyList()
    }

    //WALIDACJA

    fun validateAddTripForm(name: String, startDate: String, endDate: String, budget: String): Boolean {
        var isValid = true
        var nameErr: String? = null
        var startErr: String? = null
        var endErr: String? = null
        var budgetErr: String? = null

        if (name.isBlank()) {
            nameErr = "Nazwa wycieczki jest wymagana"; isValid = false
        }

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        var parsedStart: LocalDate? = null
        var parsedEnd: LocalDate? = null

        if (startDate.isBlank()) {
            startErr = "Data rozpoczęcia jest wymagana"; isValid = false
        } else {
            try {
                parsedStart = LocalDate.parse(startDate, dateFormatter)
            } catch (e: Exception) { startErr = "Błędny format daty"; isValid = false }
        }

        if (endDate.isBlank()) {
            endErr = "Data zakończenia jest wymagana"; isValid = false
        } else {
            try {
                parsedEnd = LocalDate.parse(endDate, dateFormatter)
            } catch (e: Exception) { endErr = "Błędny format daty"; isValid = false }
        }

        if (parsedStart != null && parsedEnd != null && parsedEnd.isBefore(parsedStart)) {
            endErr = "Data zakończenia musi być po dacie rozpoczęcia"; isValid = false
        }

        if (budget.isBlank()) {
            budgetErr = "Budżet jest wymagany"; isValid = false
        } else {
            val parsed = budget.replace(",", ".").toDoubleOrNull()
            if (parsed == null || parsed < 0) {
                budgetErr = "Podaj poprawną kwotę (np. 2000.50)"; isValid = false
            }
        }

        _addTripErrors.value = AddTripFormErrors(nameErr, startErr, endErr, budgetErr)
        return isValid
    }

    fun validateTripNodeForm(name: String, startTime: String, endTime: String, price: String): Boolean {
        var isValid = true
        var nameErr: String? = null
        var startErr: String? = null
        var endErr: String? = null
        var priceErr: String? = null

        if (name.isBlank()) {
            nameErr = "Tytuł jest wymagany"; isValid = false
        }

        val dtFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        var parsedStart: LocalDateTime? = null
        var parsedEnd: LocalDateTime? = null

        if (startTime.isBlank()) {
            startErr = "Data rozpoczęcia jest wymagana"; isValid = false
        } else {
            try {
                parsedStart = LocalDateTime.parse(startTime.trim(), dtFormatter)
            } catch (e: Exception) {
                startErr = "Format: DD.MM.YYYY HH:MM"; isValid = false
            }
        }

        if (endTime.isBlank()) {
            endErr = "Data zakończenia jest wymagana"; isValid = false
        } else {
            try {
                parsedEnd = LocalDateTime.parse(endTime.trim(), dtFormatter)
            } catch (e: Exception) {
                endErr = "Format: DD.MM.YYYY HH:MM"; isValid = false
            }
        }

        if (parsedStart != null && parsedEnd != null && parsedEnd.isBefore(parsedStart)) {
            endErr = "Koniec musi być po rozpoczęciu"; isValid = false
        }

        if (price.isNotBlank()) {
            val parsedPrice = price.replace(",", ".").toDoubleOrNull()
            if (parsedPrice == null || parsedPrice < 0) {
                priceErr = "Podaj poprawną kwotę (np. 150.00)"; isValid = false
            }
        }

        _nodeFormErrors.value = TripNodeFormErrors(nameErr, startErr, endErr, priceErr)
        return isValid
    }

    fun validateInviteForm(email: String): Boolean {
        val err = when {
            email.isBlank() -> "Adres e-mail jest wymagany"
            !emailRegex.matches(email.trim()) -> "Niepoprawny format e-mail"
            else -> null
        }
        _inviteFormErrors.value = InviteFormErrors(err)
        return err == null
    }


    private fun formatDateForApi(date: String): String {
        val parts = date.split(".")
        return if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else date
    }

    private fun formatDateTimeForApi(input: String): String {
        return try {
            val parts = input.trim().split(" ")
            val dateParts = parts[0].split(".")
            "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}T${parts[1]}:00"
        } catch (e: Exception) { input }
    }
}