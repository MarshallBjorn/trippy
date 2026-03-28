package com.navrotskyi.trippyapp.models

import com.navrotskyi.trippyapp.data.entity.User

data class Trip(
    val id: String,
    val owner: User?,
    val name: String,
    val pickedCurrency: String,
    val startDate: String,
    val endDate: String,
    val budget: Double
)
