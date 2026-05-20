package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.models.TripNodeDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

data class ReporterFilter(val id: String, val name: String)

sealed class ExpenseHistoryState {
    object Loading : ExpenseHistoryState()
    data class Success(
        val allExpenses: List<TripNodeDto>,
        val filteredExpenses: List<TripNodeDto>,
        val availableCategories: List<String>,
        val availableReporters: List<ReporterFilter>,
        val selectedCategories: Set<String>,
        val selectedReporters: Set<String>
    ) : ExpenseHistoryState()
    data class Error(val message: String) : ExpenseHistoryState()
}

class ExpenseHistoryViewModel : ViewModel() {
    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)

    private val _expenses = MutableStateFlow<List<TripNodeDto>>(emptyList())
    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedReporters = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()
    val selectedReporters: StateFlow<Set<String>> = _selectedReporters.asStateFlow()

    val uiState: StateFlow<ExpenseHistoryState> = combine(
        _expenses, _selectedCategories, _selectedReporters, _isLoading, _errorMessage
    ) { expenses, categories, reporters, loading, error ->
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
                    selectedReporters = reporters
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExpenseHistoryState.Loading
    )

    fun load(tripId: String) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            try {
                val response = api.getTripNodes(tripId)
                if (response.isSuccessful && response.body() != null) {
                    val all = response.body()!!
                    _expenses.value = all.filter { it.price > 0.0 }
                } else {
                    _errorMessage.value = "Błąd pobierania danych: ${response.code()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Błąd połączenia: ${e.localizedMessage}"
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
