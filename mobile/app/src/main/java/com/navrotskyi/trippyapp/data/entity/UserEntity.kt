package com.navrotskyi.trippyapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(
    tableName = "users",
    indices = [
        Index(
            value = ["email"],
            unique = true
        )
    ]
)
data class User(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "role")
    val role: String? = null,

    @ColumnInfo(name = "password")
    val password: String? = null,

    @ColumnInfo(name = "stringUrl")
    val stringUrl: String? = null,

    @ColumnInfo(name = "currency")
    val currency: String? = null,

    @ColumnInfo(name = "isVerified")
    val isVerified: Boolean? = null,

    @ColumnInfo(name = "isBlocked")
    val isBlocked: Boolean? = null
)