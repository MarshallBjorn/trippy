package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException



data class AddTripFormErrors(
    val nameError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
    val budgetError: String? = null
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

class TripViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)


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

    // stan akcji i formularze

    private val _addTripErrors = MutableStateFlow(AddTripFormErrors())
    val addTripErrors: StateFlow<AddTripFormErrors> = _addTripErrors.asStateFlow()

    private val _createTripState = MutableStateFlow<CreateTripState>(CreateTripState.Idle)
    val createTripState: StateFlow<CreateTripState> = _createTripState.asStateFlow()

    private val _createNodeState = MutableStateFlow<CreateTripNodeState>(CreateTripNodeState.Idle)
    val createNodeState: StateFlow<CreateTripNodeState> = _createNodeState.asStateFlow()

    private val _inviteState = MutableStateFlow<InviteState>(InviteState.Idle)
    val inviteState: StateFlow<InviteState> = _inviteState.asStateFlow()

    init {
        loadTrips()
    }

    // wycieczki

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

    // uczestnicy

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

    // node management: dodawanie, edycja usuwanie, ladowanie

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

        // Walidacja daty
        if (apiEnd < apiStart) {
            _createNodeState.value = CreateTripNodeState.Error("Data zakończenia nie może być przed rozpoczęciem!")
            return
        }

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

        if (apiEnd < apiStart) {
            _createNodeState.value = CreateTripNodeState.Error("Data zakończenia nie może być przed rozpoczęciem!")
            return
        }

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

    // resetowanie stanow i czyszczenie ekranow

    fun resetCreateTripState() { _createTripState.value = CreateTripState.Idle }
    fun resetCreateNodeState() { _createNodeState.value = CreateTripNodeState.Idle }
    fun resetCreateTripNodeState() { _createNodeState.value = CreateTripNodeState.Idle }
    fun resetInviteState() { _inviteState.value = InviteState.Idle }
    fun clearAddTripErrors() { _addTripErrors.value = AddTripFormErrors() }
    fun clearSelectedNode() { _selectedNode.value = null }
    fun clearData() {
        _trips.value = emptyList()
        _participants.value = emptyList()
        _nodes.value = emptyList()
    }

    // walidacja wycieczek

    fun validateAddTripForm(name: String, startDate: String, endDate: String, budget: String): Boolean {
        var isValid = true
        var nameErr: String? = null
        var startErr: String? = null
        var endErr: String? = null
        var budgetErr: String? = null

        if (name.isBlank()) { nameErr = "Nazwa jest wymagana"; isValid = false }

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        var parsedStart: LocalDate? = null
        var parsedEnd: LocalDate? = null

        try {
            parsedStart = LocalDate.parse(startDate, dateFormatter)
        } catch (e: Exception) { startErr = "Błędna data"; isValid = false }

        try {
            parsedEnd = LocalDate.parse(endDate, dateFormatter)
        } catch (e: Exception) { endErr = "Błędna data"; isValid = false }

        if (parsedStart != null && parsedEnd != null && parsedEnd.isBefore(parsedStart)) {
            endErr = "Koniec musi być po starcie"; isValid = false
        }

        if (budget.toDoubleOrNull() == null || budget.toDouble() < 0) {
            budgetErr = "Podaj poprawną kwotę"; isValid = false
        }

        _addTripErrors.value = AddTripFormErrors(nameErr, startErr, endErr, budgetErr)
        return isValid
    }

    // funkcje pomocnicze do formatowania dat

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