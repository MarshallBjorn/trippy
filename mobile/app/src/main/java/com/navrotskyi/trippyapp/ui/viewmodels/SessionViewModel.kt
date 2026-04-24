package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.navrotskyi.trippyapp.models.Roles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionViewModel : ViewModel() {

    private val _roles = MutableStateFlow<Roles?>(null)
    val role: StateFlow<Roles?> = _roles

    fun setRole(roles: String) {
        _roles.value = Roles.valueOf(roles)
    }

    fun clear() {
        _roles.value = null
    }
}