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
                // If tripId is null, we might want a global summary or just the first trip
                // For now, let's assume we have a way to get a general summary or we pass a specific tripId
                val response = if (tripId != null) {
                    api.getExpensesSummary(tripId)
                } else {
                    // Fallback or general summary if implemented
                    // For demo purposes, we can try to get the first trip's summary if tripId is null
                    val tripsResponse = api.getMyTrips()
                    if (tripsResponse.isSuccessful && !tripsResponse.body().isNullOrEmpty()) {
                        api.getExpensesSummary(tripsResponse.body()!![0].id)
                    } else {
                        null
                    }
                }

                if (response?.isSuccessful == true && response.body() != null) {
                    _uiState.value = ExpensesState.Success(response.body()!!)
                } else {
                    _uiState.value = ExpensesState.Error("Błąd pobierania danych: ${response?.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ExpensesState.Error("Błąd połączenia: ${e.localizedMessage}")
            }
        }
    }

    fun settle(settlementId: String) {
        // Implement settlement logic
    }
}
