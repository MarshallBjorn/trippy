package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.models.InvitationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.navrotskyi.trippyapp.api.ErrorMessages
import com.navrotskyi.trippyapp.api.RetrofitClient



class InvitationsViewModel : ViewModel() {

    private val api = RetrofitClient.retrofit.create(com.navrotskyi.trippyapp.api.TrippyApi::class.java)

    private val _invitations = MutableStateFlow<List<InvitationDto>>(emptyList())
    val invitations: StateFlow<List<InvitationDto>> = _invitations

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadInvitations() {
        viewModelScope.launch {
            try {
                val response = api.getInvitations()

                if (response.isSuccessful) {
                    _invitations.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }

            } catch (e: Exception) {
                _error.value = ErrorMessages.fromThrowable(e)
            }
        }
    }

    fun accept(id: String, onAccepted: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = api.acceptInvitation(id)

                if (response.isSuccessful) {
                    loadInvitations()
                    // Po akceptacji wycieczka powinna od razu pojawić się na liście podróży.
                    onAccepted()
                } else {
                    _error.value = "Nie udało się zaakceptować zaproszenia"
                }

            } catch (e: Exception) {
                _error.value = ErrorMessages.fromThrowable(e)
            }
        }
    }

    fun reject(id: String) {
        viewModelScope.launch {
            try {
                val response = api.rejectInvitation(id)

                if (response.isSuccessful) {
                    loadInvitations()
                } else {
                    _error.value = "Nie udało się odrzucić zaproszenia"
                }

            } catch (e: Exception) {
                _error.value = ErrorMessages.fromThrowable(e)
            }
        }
    }
}