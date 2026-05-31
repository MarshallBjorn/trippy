package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.TripNodeDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

data class ReporterFilter(val id: String, val name: String)

data class MySettlementInfo(
    val otherName: String,
    val amount: Double,
    val isIncoming: Boolean
)

data class BalanceSummary(
    val myNetBalance: Double,
    val currency: String,
    val totalSettlements: Int,
    val mySettlements: List<MySettlementInfo>
)

sealed class ExpenseHistoryState {
    object Loading : ExpenseHistoryState()
    data class Success(
        val allExpenses: List<TripNodeDto>,
        val filteredExpenses: List<TripNodeDto>,
        val availableCategories: List<String>,
        val availableReporters: List<ReporterFilter>,
        val selectedCategories: Set<String>,
        val selectedReporters: Set<String>,
        val balanceSummary: BalanceSummary?
    ) : ExpenseHistoryState()
    data class Error(val message: String) : ExpenseHistoryState()
}

class ExpenseHistoryViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)

    private val _expenses = MutableStateFlow<List<TripNodeDto>>(emptyList())
    private val _balanceSummary = MutableStateFlow<BalanceSummary?>(null)
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedReporters = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()
    val selectedReporters: StateFlow<Set<String>> = _selectedReporters.asStateFlow()

    val uiState: StateFlow<ExpenseHistoryState> = combine(
        listOf(
            _expenses, _selectedCategories, _selectedReporters,
            _isLoading, _errorMessage, _balanceSummary
        )
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val expenses = values[0] as List<TripNodeDto>
        @Suppress("UNCHECKED_CAST")
        val categories = values[1] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val reporters = values[2] as Set<String>
        val loading = values[3] as Boolean
        val error = values[4] as String?
        val balance = values[5] as BalanceSummary?

        when {
            loading -> ExpenseHistoryState.Loading
            error != null -> ExpenseHistoryState.Error(error)
            else -> {
                val filtered = expenses.filter { node ->
                    val matchesCategory = categories.isEmpty() ||
                            (node.category ?: "Inne") in categories
                    val matchesReporter = reporters.isEmpty() ||
                            node.reporterId in reporters
                    matchesCategory && matchesReporter
                }
                val availableCategories = expenses
                    .map { it.category ?: "Inne" }
                    .distinct()
                    .sorted()
                val availableReporters = expenses
                    .mapNotNull { node ->
                        val id = node.reporterId ?: return@mapNotNull null
                        val name = node.reporterName ?: id
                        ReporterFilter(id, name)
                    }
                    .distinctBy { it.id }
                    .sortedBy { it.name }
                ExpenseHistoryState.Success(
                    allExpenses = expenses,
                    filteredExpenses = filtered,
                    availableCategories = availableCategories,
                    availableReporters = availableReporters,
                    selectedCategories = categories,
                    selectedReporters = reporters,
                    balanceSummary = balance
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseHistoryState.Loading
    )

    fun load(tripId: String, currentUserId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val nodesDeferred = async { api.getTripNodes(tripId) }
                val balancesDeferred = async { api.getGroupBalances(tripId) }

                val nodesResp = nodesDeferred.await()
                if (nodesResp.isSuccessful && nodesResp.body() != null) {
                    _expenses.value = nodesResp.body()!!.filter { it.price > 0.0 }
                } else {
                    _errorMessage.value = "Błąd pobierania wydatków: ${nodesResp.code()}"
                    return@launch
                }

                val balancesResp = balancesDeferred.await()
                if (balancesResp.isSuccessful && balancesResp.body() != null) {
                    val data = balancesResp.body()!!
                    val myNet = data.balances.find { it.userId == currentUserId }?.netBalance ?: 0.0
                    val mySettlements = data.settlements.mapNotNull { s ->
                        when (currentUserId) {
                            s.fromUserId -> MySettlementInfo(s.toUserName, s.amount, isIncoming = false)
                            s.toUserId -> MySettlementInfo(s.fromUserName, s.amount, isIncoming = true)
                            else -> null
                        }
                    }
                    _balanceSummary.value = BalanceSummary(
                        myNetBalance = myNet,
                        currency = data.currencyCode,
                        totalSettlements = data.settlements.size,
                        mySettlements = mySettlements
                    )
                }
            } catch (e: Exception) {

                Log.e(
                    "ExpenseHistoryViewModel",
                    "Failed to load expense history",
                    e
                )

                _errorMessage.value =
                    "Błąd połączenia: ${e.localizedMessage}"

            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleCategory(category: String) {
        _selectedCategories.value = _selectedCategories.value.toMutableSet().also {
            if (!it.add(category)) it.remove(category)
        }
    }

    fun toggleReporter(reporterId: String) {
        _selectedReporters.value = _selectedReporters.value.toMutableSet().also {
            if (!it.add(reporterId)) it.remove(reporterId)
        }
    }

    fun clearFilters() {
        _selectedCategories.value = emptySet()
        _selectedReporters.value = emptySet()
    }
}
