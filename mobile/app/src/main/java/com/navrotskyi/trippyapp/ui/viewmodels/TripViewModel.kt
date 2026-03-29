package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.InviteParticipantRequest
import com.navrotskyi.trippyapp.models.Trip
import com.navrotskyi.trippyapp.models.TripParticipantDto
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
sealed class InviteState {
    object Idle : InviteState()
    object Loading : InviteState()
    object Success : InviteState()
    data class Error(val message: String) : InviteState()
}

class TripViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _participants = MutableStateFlow<List<TripParticipantDto>>(emptyList())
    val participants: StateFlow<List<TripParticipantDto>> = _participants.asStateFlow()

    private val _inviteState = MutableStateFlow<InviteState>(InviteState.Idle)
    val inviteState: StateFlow<InviteState> = _inviteState.asStateFlow()

    private val _addTripErrors = MutableStateFlow(AddTripFormErrors())
    val addTripErrors: StateFlow<AddTripFormErrors> = _addTripErrors.asStateFlow()

    init {
        loadTrips()
    }

    fun loadTrips() {
        viewModelScope.launch {
            try {
                val response = api.getMyTrips()
                if (response.isSuccessful && response.body() != null) {
                    _trips.value = response.body()!!.map { dto ->
                        Trip(
                            id = dto.id, // Retrofit/Gson zamieni UUID na String
                            owner = null,
                            name = dto.name,
                            pickedCurrency = dto.currencyCode,
                            startDate = dto.startDate,
                            endDate = dto.endDate,
                            budget = dto.budget
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshTrips() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadTrips()
            _isRefreshing.value = false
        }
    }

    fun loadParticipants(tripId: String) {
        viewModelScope.launch {
            try {
                val response = api.getTripParticipants(tripId)
                if (response.isSuccessful && response.body() != null) {
                    _participants.value = response.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun inviteParticipant(tripId: String, email: String) {
        _inviteState.value = InviteState.Loading
        viewModelScope.launch {
            try {
                val request = InviteParticipantRequest(
                    email = email,
                    role = "PARTICIPANT"
                )
                val response = api.inviteParticipant(tripId, request)

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

    fun clearData() {
        _trips.value = emptyList()
        _participants.value = emptyList()
    }

    fun resetInviteState() {
        _inviteState.value = InviteState.Idle
    }

    fun validateAddTripForm(name: String, startDate: String, endDate: String, budget: String): Boolean {
        var isValid = true
        var nameErr: String? = null
        var startErr: String? = null
        var endErr: String? = null
        var budgetErr: String? = null

        // Walidacja nazwy
        if (name.isBlank()) {
            nameErr = "Nazwa wycieczki jest wymagana"
            isValid = false
        }

        // Walidacja dat
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        var parsedStart: LocalDate? = null
        var parsedEnd: LocalDate? = null

        // Sprawdzanie daty rozpoczecia
        if (startDate.isBlank()) {
            startErr = "Data rozpoczęcia jest wymagana"
            isValid = false
        } else {
            try {
                parsedStart = LocalDate.parse(startDate, dateFormatter)
            } catch (e: DateTimeParseException) {
                startErr = "Niepoprawny format lub data (DD.MM.RRRR)"
                isValid = false
            }
        }

        // Sprawdzanie daty zakonczenia
        if (endDate.isBlank()) {
            endErr = "Data zakończenia jest wymagana"
            isValid = false
        } else {
            try {
                parsedEnd = LocalDate.parse(endDate, dateFormatter)
            } catch (e: DateTimeParseException) {
                endErr = "Niepoprawny format lub data (DD.MM.RRRR)"
                isValid = false
            }
        }

        // PORÓWNANIE DAT
        if (parsedStart != null && parsedEnd != null) {
            if (parsedEnd.isBefore(parsedStart)) {
                endErr = "Data zakończenia musi być po dacie rozpoczęcia"
                isValid = false
            }
        }

        // Walidacja budżetu
        if (budget.isBlank()) {
            budgetErr = "Budżet jest wymagany"
            isValid = false
        } else if (budget.toDoubleOrNull() == null || budget.toDouble() < 0) {
            budgetErr = "Podaj poprawną kwotę (np. 2000.50)"
            isValid = false
        }


        _addTripErrors.value = AddTripFormErrors(nameErr, startErr, endErr, budgetErr)
        return isValid
    }

    fun clearAddTripErrors() {
        _addTripErrors.value = AddTripFormErrors()
    }
}


