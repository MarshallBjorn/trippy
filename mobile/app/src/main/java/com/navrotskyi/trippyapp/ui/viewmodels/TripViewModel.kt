package com.navrotskyi.trippyapp.ui.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.navrotskyi.trippyapp.api.ErrorMessages
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.data.database.TrippyDb
import com.navrotskyi.trippyapp.data.network.NetworkMonitor
import com.navrotskyi.trippyapp.data.repository.TripRepository
import com.navrotskyi.trippyapp.data.sync.SyncManager
import com.navrotskyi.trippyapp.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach


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

/** Wynik uniwersalnej operacji zapisu z fallbackiem offline. */
sealed class NetworkResult {
    /** Żądanie wysłane i potwierdzone przez serwer (2xx). */
    object Success : NetworkResult()
    /** Brak sieci — operacja trafiła do kolejki offline. */
    object Queued : NetworkResult()
    /** Serwer odrzucił żądanie (np. 4xx/5xx). */
    data class ServerError(val code: Int) : NetworkResult()
}

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val api by lazy { RetrofitClient.retrofit.create(TrippyApi::class.java) }
    private val db = TrippyDb.getInstance(application)
    private val repo = TripRepository(api, db.tripDao(), db.tripNodeDao())

    private var nodesObserveJob: Job? = null
    private var nodeObserveJob: Job? = null

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    //  SSOT: trips obserwowane z Room
    val trips: StateFlow<List<Trip>> = repo.observeTrips()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Stany sieci / synchronizacji
    val isOnline: StateFlow<Boolean> = NetworkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val pendingSyncCount: StateFlow<Int> = SyncManager.pendingCount
    val isSyncing: StateFlow<Boolean> = SyncManager.isSyncing

    private val _isLoadingTrips = MutableStateFlow(false)
    val isLoadingTrips: StateFlow<Boolean> = _isLoadingTrips.asStateFlow()

    private val _isLoadingNodes = MutableStateFlow(false)
    val isLoadingNodes: StateFlow<Boolean> = _isLoadingNodes.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    // node
    private val _currentTripIdForNodes = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val nodes: StateFlow<List<TripNodeDto>> = _currentTripIdForNodes
        .onEach { android.util.Log.d("TripVM", "[1] _currentTripIdForNodes = $it") }
        .filterNotNull()
        .onEach { android.util.Log.d("TripVM", "[2] filterNotNull passed tripId=$it") }
        .flatMapLatest { tripId ->
            android.util.Log.d("TripVM", "[3] flatMapLatest start observeNodes($tripId)")
            repo.observeNodes(tripId)
        }
        .onEach { android.util.Log.d("TripVM", "[4] nodes EMIT ${it.size} items") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    val expenses: StateFlow<List<TripNodeDto>> = nodes

    private val _selectedNodeId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedNode: StateFlow<TripNodeDto?> = _selectedNodeId
        .filterNotNull()
        .flatMapLatest { nodeId -> repo.observeNode(nodeId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Pozostałe stany
    private val _participants = MutableStateFlow<List<TripParticipantDto>>(emptyList())
    val participants: StateFlow<List<TripParticipantDto>> = _participants.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Formularze
    private val _addTripErrors = MutableStateFlow(AddTripFormErrors())
    val addTripErrors: StateFlow<AddTripFormErrors> = _addTripErrors.asStateFlow()

    private val _nodeFormErrors = MutableStateFlow(TripNodeFormErrors())
    val nodeFormErrors: StateFlow<TripNodeFormErrors> = _nodeFormErrors.asStateFlow()

    private val _inviteFormErrors = MutableStateFlow(InviteFormErrors())
    val inviteFormErrors: StateFlow<InviteFormErrors> = _inviteFormErrors.asStateFlow()

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
            _isLoadingTrips.value = true
            repo.refreshTrips()
                .onSuccess { _isOffline.value = false }
                .onFailure { _isOffline.value = true }
            _isLoadingTrips.value = false
        }
    }

    fun refreshTrips() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repo.refreshTrips()
                .onSuccess { _isOffline.value = false }
                .onFailure { _isOffline.value = true }
            _isRefreshing.value = false
        }
    }

    fun createTrip(name: String, startDate: String, endDate: String, budget: String, currency: String) {
        _createTripState.value = CreateTripState.Loading

        val request = CreateTripEventRequest(
            name = name,
            currencyCode = currency,
            startDate = formatDateForApi(startDate),
            endDate = formatDateForApi(endDate),
            budget = budget.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            val result = executeOrEnqueue(
                apiCall = { api.createTrip(request) },
                enqueue = { SyncManager.enqueueCreateTrip(request) },
                onSuccess = { repo.refreshTrips() }
            )
            _createTripState.value = when (result) {
                is NetworkResult.ServerError -> CreateTripState.Error("Błąd serwera: ${result.code}")
                else -> CreateTripState.Success
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
            } catch (e: Exception) {

                android.util.Log.e(
                    "TripViewModel",
                    "loadParticipants failed",
                    e
                )
            }
            } catch (e: Exception) {

                android.util.Log.e(
                    "TripViewModel",
                    "loadParticipants failed",
                    e
                )
            }
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
                android.util.Log.e(
                    "TripViewModel",
                    "inviteParticipant failed",
                    e
                )
                android.util.Log.e(
                    "TripViewModel",
                    "inviteParticipant failed",
                    e
                )
            }
        }
    }

    // NODES
    fun loadNodes(tripId: String) {
        android.util.Log.d("TripVM", "[0] loadNodes($tripId) wywolane")
        _currentTripIdForNodes.value = tripId
        viewModelScope.launch {
            _isLoadingNodes.value = true
            val result = repo.refreshNodes(tripId)
            android.util.Log.d("TripVM", "[R] refreshNodes wynik: $result")
            result
                .onSuccess { _isOffline.value = false }
                .onFailure { _isOffline.value = true }
            _isLoadingNodes.value = false
        }
    }

    fun loadNode(tripId: String, nodeId: String) {
        _selectedNodeId.value = nodeId
        viewModelScope.launch {
            repo.refreshNode(tripId, nodeId)
                .onSuccess { _isOffline.value = false }
                .onFailure { _isOffline.value = true }
        }
    }

    fun createTripNode(
        tripId: String, name: String, startTime: String, endTime: String,
        price: String, note: String, separate: Boolean, category: String = "Inne"
    ) {
        _createNodeState.value = CreateTripNodeState.Loading

        val apiStart = formatDateTimeForApi(startTime)
        val apiEnd = formatDateTimeForApi(endTime)

        if (apiEnd < apiStart) {
            _createNodeState.value = CreateTripNodeState.Error("Data zakończenia nie może być przed rozpoczęciem!")
            return
        }

        val formattedPrice = price.replace(",", ".").toDoubleOrNull() ?: 0.0
        val request = CreateTripNodeRequest(
            name = name,
            startTime = apiStart,
            endTime = apiEnd,
            note = note.ifBlank { null },
            price = formattedPrice,
            separate = separate
            separate = separate
        )

        viewModelScope.launch {
            val result = executeOrEnqueue(
                apiCall = { api.createTripNode(tripId, request) },
                enqueue = { SyncManager.enqueueCreateNode(tripId, request) },
                onSuccess = {
                    repo.refreshNodes(tripId)
                    repo.refreshTrips()
                }
            )
            _createNodeState.value = when (result) {
                is NetworkResult.ServerError -> CreateTripNodeState.Error("Błąd serwera: ${result.code}")
                else -> CreateTripNodeState.Success
            }
        }
    }

    fun updateTripNode(
        tripId: String, nodeId: String, name: String, startTime: String, endTime: String,
        price: String, note: String, separate: Boolean, category: String
    ) {
        _createNodeState.value = CreateTripNodeState.Loading

        val apiStart = formatDateTimeForApi(startTime)
        val apiEnd = formatDateTimeForApi(endTime)

        if (apiEnd < apiStart) {
            _createNodeState.value = CreateTripNodeState.Error("Data zakończenia nie może być przed rozpoczęciem!")
            return
        }

        val formattedPrice = price.replace(",", ".").toDoubleOrNull() ?: 0.0
        val request = CreateTripNodeRequest(
            name = name,
            startTime = apiStart,
            endTime = apiEnd,
            note = note.ifBlank { null },
            price = formattedPrice,
            separate = separate
            separate = separate
        )

        viewModelScope.launch {
            val result = executeOrEnqueue(
                apiCall = { api.updateTripNode(tripId, nodeId, request) },
                enqueue = { SyncManager.enqueueUpdateNode(tripId, nodeId, request) },
                onSuccess = {
                    repo.refreshNodes(tripId)
                    repo.refreshNode(tripId, nodeId)
                    repo.refreshTrips()
                }
            )
            _createNodeState.value = when (result) {
                is NetworkResult.ServerError -> CreateTripNodeState.Error("Błąd serwera: ${result.code}")
                else -> CreateTripNodeState.Success
            }
        }
    }

    fun deleteTripNode(tripId: String, nodeId: String) {
        viewModelScope.launch {
            executeOrEnqueue(
                apiCall = { api.deleteTripNode(tripId, nodeId) },
                enqueue = { SyncManager.enqueueDeleteNode(tripId, nodeId) },
                onSuccess = {
                    repo.refreshNodes(tripId)
                    repo.refreshTrips()
                    repo.refreshTrips()
                }
            )
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
            } catch (e: Exception) {

                android.util.Log.e(
                    "TripViewModel",
                    "loadPosts failed",
                    e
                )
            }
            } catch (e: Exception) {

                android.util.Log.e(
                    "TripViewModel",
                    "loadPosts failed",
                    e
                )
            }
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
                android.util.Log.e(
                    "TripViewModel",
                    "createPost failed",
                    e
                )
                android.util.Log.e(
                    "TripViewModel",
                    "createPost failed",
                    e
                )
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
    fun clearSelectedNode() { _selectedNodeId.value = null }

    fun clearData() {
        _participants.value = emptyList()
        _currentTripIdForNodes.value = null
        _selectedNodeId.value = null
        viewModelScope.launch { repo.clearCache() }
    }

    // WALIDACJA

    fun validateAddTripForm(name: String, startDate: String, endDate: String, budget: String): Boolean {
        var isValid = true
        var nameErr: String? = null
        var startErr: String? = null
        var endErr: String? = null
        var budgetErr: String? = null

        if (name.isBlank()) { nameErr = "Nazwa wycieczki jest wymagana"; isValid = false }

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        var parsedStart: LocalDate? = null
        var parsedEnd: LocalDate? = null

        if (startDate.isBlank()) {
            startErr = "Data rozpoczęcia jest wymagana"; isValid = false
        } else {
            try { parsedStart = LocalDate.parse(startDate, dateFormatter) }
            catch (e: DateTimeParseException) { startErr = "Błędny format daty (DD.MM.RRRR)"; isValid = false }
        }

        if (endDate.isBlank()) {
            endErr = "Data zakończenia jest wymagana"; isValid = false
        } else {
            try { parsedEnd = LocalDate.parse(endDate, dateFormatter) }
            catch (e: DateTimeParseException) { endErr = "Błędny format daty (DD.MM.RRRR)"; isValid = false }
        }

        if (parsedStart != null && parsedEnd != null && !parsedEnd.isAfter(parsedStart)) {
            endErr = "Data zakończenia musi być po dacie rozpoczęcia"; isValid = false
        }

        if (budget.isBlank()) {
            budgetErr = "Budżet jest wymagany"; isValid = false
        } else {
            val budgetValue = budget.replace(",", ".").toDoubleOrNull()
            if (budgetValue == null || budgetValue < 0) {
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

        if (name.isBlank()) { nameErr = "Tytuł jest wymagany"; isValid = false }

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        var parsedStart: LocalDateTime? = null
        var parsedEnd: LocalDateTime? = null

        if (startTime.isBlank()) {
            startErr = "Data i godzina rozpoczęcia są wymagane"; isValid = false
        } else {
            try { parsedStart = LocalDateTime.parse(startTime, formatter) }
            catch (e: DateTimeParseException) { startErr = "Błędny format (DD.MM.RRRR HH:MM)"; isValid = false }
        }

        if (endTime.isBlank()) {
            endErr = "Data i godzina zakończenia są wymagane"; isValid = false
        } else {
            try { parsedEnd = LocalDateTime.parse(endTime, formatter) }
            catch (e: DateTimeParseException) { endErr = "Błędny format (DD.MM.RRRR HH:MM)"; isValid = false }
        }

        if (parsedStart != null && parsedEnd != null && !parsedEnd.isAfter(parsedStart)) {
            endErr = "Koniec musi być po rozpoczęciu"; isValid = false
        }

        if (price.isNotBlank()) {
            val trimmed = price.trim()
            val priceValue = trimmed.replace(",", ".").toDoubleOrNull()
            if (priceValue == null || priceValue < 0 || trimmed.startsWith("-")) {
                priceErr = "Podaj poprawną kwotę (np. 150.00)"; isValid = false
            }
        }

        _nodeFormErrors.value = TripNodeFormErrors(nameErr, startErr, endErr, priceErr)
        return isValid
    }

    fun validateInviteForm(email: String): Boolean {
        val emailErr = when {
            email.isBlank() -> "Adres e-mail jest wymagany"
            !emailRegex.matches(email) -> "Niepoprawny format e-mail"
            else -> null
        }
        _inviteFormErrors.value = InviteFormErrors(emailErr)
        return emailErr == null
    }

    // FUNKCJE POMOCNICZE

    /**
     * Uniwersalna obsługa zapisu (create/update/delete) z fallbackiem offline.
     *
     * Najpierw PRÓBUJE wysłać żądanie. Dopiero realny błąd sieci (wyjątek) powoduje
     * zakolejkowanie operacji do późniejszej synchronizacji — dzięki temu przy
     * działającym API dane nie lądują niepotrzebnie w "Oczekiwaniu na zmiany".
     *
     * - 2xx                  → [onSuccess] + [NetworkResult.Success]
     * - błąd serwera (!2xx)  → [NetworkResult.ServerError]
     * - brak sieci (wyjątek) → [enqueue] + [NetworkResult.Queued]
     */
    private suspend fun <T> executeOrEnqueue(
        apiCall: suspend () -> Response<T>,
        enqueue: suspend () -> Unit,
        onSuccess: suspend (T?) -> Unit = {}
    ): NetworkResult {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                onSuccess(response.body())
                NetworkResult.Success
            } else {
                NetworkResult.ServerError(response.code())
            }
        } catch (e: Exception) {
            android.util.Log.e("TripViewModel", "Żądanie sieciowe nieudane — kolejkuję offline", e)
            enqueue()
            NetworkResult.Queued
        }
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