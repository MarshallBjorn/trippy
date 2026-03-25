package com.navrotskyi.trippyapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.entity.User
import com.navrotskyi.trippyapp.ui.screens.profile.CurrencyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userDao: UserDao) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _currencies = MutableStateFlow<List<CurrencyItem>>(emptyList())
    val currencies: StateFlow<List<CurrencyItem>> = _currencies.asStateFlow()

    init {
        observeUserData()
        fetchCurrenciesFromDb()
    }

    private fun observeUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.getAll().collect { users ->
                _user.value = users.firstOrNull()
            }
        }
    }

    private fun fetchCurrenciesFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: Pobranie listy walut
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _user.value?.let { currentUser ->
                userDao.update(listOf(currentUser.copy(name = newName)))
                // TODO: Synchronizacja z API
            }
        }
    }

    fun updateCurrency(newCurrencyCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _user.value?.let { currentUser ->
                userDao.update(listOf(currentUser.copy(currency = newCurrencyCode)))
                // TODO: Synchronizacja z API
            }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _user.value?.let { currentUser ->
                userDao.update(listOf(currentUser.copy(password = newPassword)))
                // TODO: Synchronizacja z API
            }
        }
    }
}

class ProfileViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}