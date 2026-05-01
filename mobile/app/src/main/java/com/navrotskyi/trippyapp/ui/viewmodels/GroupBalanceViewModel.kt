package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.ui.screens.ParticipantBalance
import com.navrotskyi.trippyapp.ui.screens.Settlement
import com.navrotskyi.trippyapp.ui.screens.SettlementType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


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

    private val _uiState = MutableStateFlow<GroupBalanceState>(GroupBalanceState.Loading)
    val uiState: StateFlow<GroupBalanceState> = _uiState.asStateFlow()

    fun loadBalancesForTrip (tripId: String) {
        viewModelScope.launch {
            _uiState.value = GroupBalanceState.Loading

            delay(1500)

            // val response = trippyApi.getGroupBalances(tripId)
            // if (response.isSuccessful) { ... }

            val mockParticipants = listOf (
                ParticipantBalance("Ty", -45.50, "Ty", avatarUrl = "https://i.pravatar.cc/150?img=68", isMe = true),
                ParticipantBalance("Tomasz", 120.00, "T"),
                ParticipantBalance("Ania", -75.00, "A"),
                ParticipantBalance("Marek", 0.50, "M"),
                ParticipantBalance("Kasia", 0.00, "K")
            )

            val mockSettlements = listOf(
                Settlement("Tomaszowi", 45.50, SettlementType.I_OWE_THEM),
                Settlement("Ania", 20.00, SettlementType.THEY_OWE_ME)
            )

            _uiState.value = GroupBalanceState.Success(
                totalBalance = -45.50,
                participants = mockParticipants,
                settlements = mockSettlements
            )
        }
    }
}