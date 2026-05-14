package com.navrotskyi.trippyapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "ownerId")
    val ownerId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "pickedCurrency")
    val pickedCurrency: String,

    @ColumnInfo(name = "startDate")
    val startDate: String,

    @ColumnInfo(name = "endDate")
    val endDate: String,

    @ColumnInfo(name = "budget")
    val budget: Double,

    // znacznik kiedy ostatni raz cache był odświeżony z API
    @ColumnInfo(name = "lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
)