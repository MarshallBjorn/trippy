package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.ExpensesSummaryDto
import com.navrotskyi.trippyapp.models.SettlementDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ExpensesState {
    object Loading : ExpensesState()
    data class Success(val summary: ExpensesSummaryDto) : ExpensesState()
    data class Error(val message: String) : ExpensesState()
}

class ExpensesViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)

    private val _uiState = MutableStateFlow<ExpensesState>(ExpensesState.Loading)
    val uiState: StateFlow<ExpensesState> = _uiState.asStateFlow()

    fun loadExpenses(tripId: String? = null) {
        _uiState.value = ExpensesState.Loading
        viewModelScope.launch {
            try {
                val targetTripId = if (tripId != null) {
                    tripId
                } else {
                    val tripsResponse = api.getMyTrips()
                    if (tripsResponse.isSuccessful && !tripsResponse.body().isNullOrEmpty()) {
                        tripsResponse.body()!![0].id
                    } else {
                        null
                    }
                }

                if (targetTripId == null) {
                    _uiState.value = ExpensesState.Error("Brak wybranej wycieczki")
                    return@launch
                }

                val response = api.getTripNodes(targetTripId)

                if (response.isSuccessful && response.body() != null) {
                    val nodes = response.body()!!
                    val totalSpent = nodes.sumOf { it.price }
                    val categoryBreakdown = nodes.groupBy { it.category ?: "Inne" }
                        .mapValues { entry -> entry.value.sumOf { it.price } }

                    val summary = ExpensesSummaryDto(
                        totalSpent = totalSpent,
                        currency = "PLN",
                        categoryBreakdown = categoryBreakdown,
                        userBalance = 0.0,
                        pendingSettlements = emptyList()
                    )

                    _uiState.value = ExpensesState.Success(summary)
                } else {
                    _uiState.value = ExpensesState.Error("Błąd pobierania danych: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ExpensesState.Error("Błąd połączenia: ${e.localizedMessage}")
            }
        }
    }

    fun settle(settlementId: String) {
        // logika rozdzielenia wydatkow
    }
}
