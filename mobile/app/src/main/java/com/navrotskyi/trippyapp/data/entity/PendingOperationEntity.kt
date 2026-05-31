package com.navrotskyi.trippyapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "tripId")
    val tripId: String? = null,

    @ColumnInfo(name = "nodeId")
    val nodeId: String? = null,

    @ColumnInfo(name = "payload")
    val payload: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "retryCount")
    val retryCount: Int = 0
)