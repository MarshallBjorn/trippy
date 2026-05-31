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
import kotlinx.coroutines.runBlocking
import android.util.Log

sealed class ExpensesState {
    object Loading : ExpensesState()
    data class Success(val summary: ExpensesSummaryDto) : ExpensesState()
    data class Error(val message: String) : ExpensesState()
}

class ExpensesViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)
    private var cachedUserId: String? = null

    val currentUserId: String
        get() {

            if (cachedUserId != null) {
                return cachedUserId!!
            }

            return try {

                runBlocking {

                    val response =
                        api.getCurrentUser()

                    if (
                        response.isSuccessful &&
                        response.body() != null
                    ) {

                        cachedUserId =
                            response.body()!!.id

                        cachedUserId!!
                    } else {
                        ""
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    "ExpensesViewModel",
                    "Failed to get current user",
                    e
                )

                ""
            }
        }
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

                val response =
                    api.getTripBalances(
                        targetTripId
                    )

                if (
                    response.isSuccessful &&
                    response.body() != null
                ) {

                    _uiState.value =
                        ExpensesState.Success(
                            response.body()!!
                        )

                } else {

                    _uiState.value =
                        ExpensesState.Error(
                            "Błąd pobierania danych: ${response.code()}"
                        )
                }
            } catch (e: Exception) {

                Log.e(
                    "ExpensesViewModel",
                    "Failed to load expenses",
                    e
                )

                _uiState.value =
                    ExpensesState.Error(
                        "Błąd połączenia: ${e.localizedMessage}"
                    )
            }
        }
    }

    fun settle(settlementId: String) {
        // logika rozdzielenia wydatkow
    }
}
