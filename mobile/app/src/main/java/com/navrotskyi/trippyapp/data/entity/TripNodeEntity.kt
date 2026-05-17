package com.navrotskyi.trippyapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_nodes",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class TripNodeEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "tripId")
    val tripId: String,

    @ColumnInfo(name = "reporterId")
    val reporterId: String? = null,

    @ColumnInfo(name = "reporterName")
    val reporterName: String? = null,

    @ColumnInfo(name = "startTime")
    val startTime: String? = null,

    @ColumnInfo(name = "endTime")
    val endTime: String? = null,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "price")
    val price: Double = 0.0,

    @ColumnInfo(name = "separate")
    val separate: Boolean = false,

    @ColumnInfo(name = "category")
    val category: String? = "Inne",

    @ColumnInfo(name = "canEdit")
    val canEdit: Boolean = false,

    @ColumnInfo(name = "canDelete")
    val canDelete: Boolean = false,

    @ColumnInfo(name = "lastUpdated")
    val lastUpdated: Long = System.currentTimeMillis()
)