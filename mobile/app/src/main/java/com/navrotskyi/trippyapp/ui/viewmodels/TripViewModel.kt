package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.data.entity.User
import com.navrotskyi.trippyapp.models.Trip
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel : ViewModel() {
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        val currentUser = User(id = 1, name = "Dawid")
        val otherUser = User(id = 2, name = "Anna")

        _trips.value = listOf(
            Trip(
                id = "1",
                owner = currentUser,
                name = "Tatry 2026",
                startDate = "10.08.2026",
                endDate = "15.08.2026",
                budget = 2500.0,
                pickedCurrency = "PLN"
            ),
            Trip(
                id = "2",
                owner = otherUser,
                name = "Wakacje w Grecji",
                startDate = "15.07.2026",
                endDate = "29.07.2026",
                budget = 8500.0,
                pickedCurrency = "EUR"
            ),
            Trip(
                id = "3",
                owner = currentUser,
                name = "Weekend w Krakowie",
                startDate = "05.05.2026",
                endDate = "07.05.2026",
                budget = 800.0,
                pickedCurrency = "PLN"
            ),
            Trip(
                id = "4",
                owner = otherUser,
                name = "Wycieczka do Zakopanego",
                startDate = "20.12.2025",
                endDate = "27.12.2025",
                budget = 3200.0,
                pickedCurrency = "PLN"
            )
        )
    }

    // Symulacja pobierania danych z API
    fun refreshTrips() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(1500)
            loadTrips()
            _isRefreshing.value = false
        }
    }
}
