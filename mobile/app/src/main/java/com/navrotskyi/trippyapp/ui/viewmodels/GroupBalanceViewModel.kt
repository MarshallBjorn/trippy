package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.api.RetrofitClient
import com.navrotskyi.trippyapp.ui.screens.ParticipantBalance
import com.navrotskyi.trippyapp.ui.screens.Settlement
import com.navrotskyi.trippyapp.ui.screens.SettlementType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.navrotskyi.trippyapp.models.*
import com.navrotskyi.trippyapp.api.TrippyApi
import android.util.Log

sealed class GroupBalanceState {
    object Loading : GroupBalanceState()
    data class Success(
        val totalBalance: Double,
        val participants: List<ParticipantBalance>,
        val settlements: List<Settlement>
    ) : GroupBalanceState()
    data class Error(val message: String) : GroupBalanceState()
}

class GroupBalanceViewModel : ViewModel() {

    private val api = RetrofitClient.retrofit.create(TrippyApi::class.java)
    private val _uiState = MutableStateFlow<GroupBalanceState>(GroupBalanceState.Loading)
    val uiState: StateFlow<GroupBalanceState> = _uiState.asStateFlow()

    fun loadBalancesForTrip(tripId: String, currentUserId: String) {
        viewModelScope.launch {
            _uiState.value = GroupBalanceState.Loading

            try {
                val response = api.getGroupBalances(tripId)

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null) {
                        val mappedParticipants = data.balances.map { dto ->
                            ParticipantBalance(
                                name = dto.userName,
                                balance = dto.netBalance,
                                initials = getInitials(dto.userName),
                                avatarUrl = null,
                                isMe = dto.userId == currentUserId
                            )
                        }.sortedByDescending { it.balance }

                        val mySettlements = mutableListOf<Settlement>()

                        data.settlements.forEach { settlement ->
                            if (settlement.fromUserId == currentUserId) {
                                mySettlements.add(
                                    Settlement(
                                        otherPersonName = settlement.toUserName,
                                        amount = settlement.amount,
                                        type = SettlementType.I_OWE_THEM
                                    )
                                )
                            } else if (settlement.toUserId == currentUserId) {
                                mySettlements.add(
                                    Settlement(
                                        otherPersonName = settlement.fromUserName,
                                        amount = settlement.amount,
                                        type = SettlementType.THEY_OWE_ME
                                    )
                                )
                            }
                        }

                        val myNetBalance = data.balances.find { it.userId == currentUserId }?.netBalance ?: 0.0

                        _uiState.value = GroupBalanceState.Success(
                            totalBalance = myNetBalance,
                            participants = mappedParticipants,
                            settlements = mySettlements
                        )
                    } else {
                        _uiState.value = GroupBalanceState.Error("Otrzymano puste dane z serwera.")
                    }
                } else {
                    _uiState.value = GroupBalanceState.Error("Nie udało się pobrać rozliczeń.")
                }
            } catch (e: Exception) {

                Log.e(
                    "GroupBalanceViewModel",
                    "Failed to load balances",
                    e
                )

                _uiState.value =
                    GroupBalanceState.Error(
                        "Błąd sieci: ${e.message}"
                    )
            }
        }
    }

    private fun getInitials(name: String): String {
        if (name.isBlank()) return "?"
        val parts = name.trim().split(" ")
        return if (parts.size >= 2) {
            "${parts[0].first()}${parts[1].first()}".uppercase()
        } else {
            parts[0].take(2).uppercase()
        }
    }
}